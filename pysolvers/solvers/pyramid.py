"""Find out how to 'clear the board' in Pyramid Solitaire.

The design is meant to be simple to understand so it is less likely to have
bugs, but to make Pyramid Solitaire solvable for the worst case scenarios, we
must do a bit of optimization work on the state representation.

This implementation skips all of the precalculations of the Java/Lisp versions
to keep things as simple as possible while still using the optimization of
cramming the entire state into an integer value.

This still needs more than 8GB RAM in the worst case because this algorithm
skips some of the features like unwinnable state detection."""
import collections

import solvers.deck


def card_value(card):
    """Return the card's numeric value according to Pyramid Solitaire rules.

    Aces are always 1, Jacks are 11, Queens are 12, and Kings are 13."""
    return 1 + "A23456789TJQK".index(solvers.deck.card_rank(card))


def cards_are_removable(card1, card2=None):
    """Return true if the card or cards can be removed together.

    Kings can be removed by themselves, and pairs of cards that add to 13."""
    values = [card_value(c) if c else 0 for c in [card1, card2]]
    return sum(values) == 13


class State:
    """A state in Pyramid Solitaire, represented by a 60-bit integer value.

    This class only has static methods, meant to be called on integer values.
    The reason is to save as much memory as possible (we'll be creating tens
    of millions of these).

    It's tempting to represent the state as lists of cards in the tableau,
    stock, and waste piles, but it's too slow and memory usage is too high.

    The trick to this state representation is that it holds data that refers
    to the deck of cards, without containing a reference to the deck. So we
    need the deck of cards to understand the state of the game.

    Bits  0-51: "deck_flags" - 52 bits representing whether or not each card
                in the deck remains in the game.
    Bits 52-57: "stock_index" - 6 bits containing a number from 28 to 52,
                an index into the deck for the card at the top of the stock
                pile. Cards with index higher than this are the remainder of
                the stock pile. Cards with index below this (and above 27) are
                the cards in the waste pile. Hint for understanding how it
                works: incrementing this stock index moves the top card of the
                stock pile to the top of the waste pile.
    Bits 58-59: 2 bits to indicate how many times the waste pile has been
                recycled."""

    EMPTY_STOCK = 52
    EMPTY_WASTE = 27
    INITIAL_STATE = (28 << 52) | ((2**52) - 1)

    # bits set on the Nth tableau card and the cards covering it from below
    UNCOVERED_MASKS = [
        0b1111111111111111111111111111,
        0b0111111011111011110111011010,
        0b1111110111110111101110110100,
        0b0011111001111001110011001000,
        0b0111110011110011100110010000,
        0b1111100111100111001100100000,
        0b0001111000111000110001000000,
        0b0011110001110001100010000000,
        0b0111100011100011000100000000,
        0b1111000111000110001000000000,
        0b0000111000011000010000000000,
        0b0001110000110000100000000000,
        0b0011100001100001000000000000,
        0b0111000011000010000000000000,
        0b1110000110000100000000000000,
        0b0000011000001000000000000000,
        0b0000110000010000000000000000,
        0b0001100000100000000000000000,
        0b0011000001000000000000000000,
        0b0110000010000000000000000000,
        0b1100000100000000000000000000,
        0b0000001000000000000000000000,
        0b0000010000000000000000000000,
        0b0000100000000000000000000000,
        0b0001000000000000000000000000,
        0b0010000000000000000000000000,
        0b0100000000000000000000000000,
        0b1000000000000000000000000000,
    ]

    @staticmethod
    def deck_flags(state):
        """Return the state's deck flags."""
        return state & 0xFFFFFFFFFFFFF

    @staticmethod
    def is_tableau_empty(state):
        return (state & 0xFFFFFFF) == 0

    @staticmethod
    def stock_index(state):
        """Return the state's stock index, the top card of the stock pile.

        If the stock index is 52, it means the stock pile is empty."""
        return (state >> 52) & 0b111111

    @staticmethod
    def cycle(state):
        """Return the state's cycle, the times the waste pile was recycled."""
        return (state >> 58) & 0b11

    @staticmethod
    def waste_index(state):
        """Return the state's waste index, the top card of the waste pile.

        If the waste index is 27, it means the waste pile is empty."""
        index = State.stock_index(state) - 1
        mask = 1 << index
        while index > State.EMPTY_WASTE:
            if (state & mask) != 0:
                break
            mask >>= 1
            index -= 1
        return index

    @staticmethod
    def _adjust_stock_index(state):
        """Return the state with its stock index adjusted correctly.

        Basically the stock index must point to a card that remains in the
        game or else be 52 to indicate the stock pile is empty. This makes sure
        every state has a single unique representation - you can't have two
        states that are effectively the same but have different stock indexes
        because one points to the actual top card and the other points to
        some card that no longer remains in the game."""
        index = State.stock_index(state)
        state = state & 0xC0FFFFFFFFFFFFF  # remove the stock index
        mask = 1 << index
        while index < State.EMPTY_STOCK:
            if (state & mask) != 0:
                break
            mask <<= 1
            index += 1
        return state | (index << 52)

    @staticmethod
    def _uncovered_indexes(deck_flags):
        """Return deck indexes of uncovered tableau cards."""
        flags = deck_flags & 0xFFFFFFF

        def is_uncovered(index):
            return (1 << index) == (flags & State.UNCOVERED_MASKS[index])

        return [i for i in range(28) if is_uncovered(i)]

    @staticmethod
    def successors(state, deck):
        """Return a list of successor states to this state.

        Actions that can be performed (if applicable):
        1. Recycle the waste pile.
        2. Draw a card from the stock pile to the waste pile.
        3. Remove a King from the tableau.
        4. Remove a King from the stock pile.
        5. Remove a King from the waste pile.
        6. Remove a pair of cards from the tableau.
        7. Remove a pair of cards, one each from the tableau and stock pile.
        8. Remove a pair of cards, one each from the tableau and waste pile.
        9. Remove a pair of cards, one each from the stock and waste piles."""

        def remove(deck_flags, *indexes):
            """Remove the cards at the indexes from the deck_flags value."""
            for index in indexes:
                deck_flags ^= (1 << index)
            return deck_flags

        results = []
        deck_flags = State.deck_flags(state)
        uncovered = State._uncovered_indexes(deck_flags)
        stock_index = State.stock_index(state)
        waste_index = State.waste_index(state)
        cycle = State.cycle(state)

        def create(deck_flags=deck_flags, stock_index=stock_index, cycle=cycle):
            """Create a new state given the individual parts of the state."""
            new_state = (cycle << 58) | (stock_index << 52) | deck_flags
            return State._adjust_stock_index(new_state)

        is_stock_empty = stock_index == State.EMPTY_STOCK
        is_waste_empty = waste_index == State.EMPTY_WASTE
        stock_card = deck[stock_index] if not is_stock_empty else None
        waste_card = deck[waste_index] if not is_waste_empty else None
        has_both = stock_card and waste_card

        if not stock_card and cycle < 2:
            # 1. recycle the waste pile
            results.append(create(stock_index=28, cycle=cycle+1))
        if stock_card:
            # 2. draw a card from stock to waste
            results.append(create(stock_index=stock_index+1))
        if stock_card and cards_are_removable(stock_card):
            # 4. remove a King from the stock pile
            results.append(create(deck_flags=remove(deck_flags, stock_index)))
        if waste_card and cards_are_removable(waste_card):
            # 5. remove a King from the waste pile
            results.append(create(remove(deck_flags, waste_index)))
        if has_both and cards_are_removable(stock_card, waste_card):
            # 9. remove the cards on the stock and waste piles
            results.append(create(remove(deck_flags, stock_index, waste_index)))
        for i in uncovered:
            if cards_are_removable(deck[i]):
                # 3. remove a King from the tableau
                results.append(create(remove(deck_flags, i)))
            else:
                if stock_card and cards_are_removable(deck[i], stock_card):
                    # 7. remove the cards from the tableau/stock pile
                    results.append(create(remove(deck_flags, i, stock_index)))
                if waste_card and cards_are_removable(deck[i], waste_card):
                    # 8. remove the cards from the tableau/waste pile
                    results.append(create(remove(deck_flags, i, waste_index)))
                for j in uncovered:
                    if cards_are_removable(deck[i], deck[j]):
                        # 6. remove two cards from the tableau
                        results.append(create(remove(deck_flags, i, j)))
        return results


def path(state, seen_states, deck):
    """Return the actions to take to get to this state from the start."""

    def is_bit_set(bits, n):
        """Return true if the nth bit of bits is equal to 1."""
        return (bits & (1 << n)) != 0

    def action(state, next_state):
        """Return the action taken to go from state to next_state."""
        diffs = state ^ next_state  # XOR to see which bits changed
        deck_diff = State.deck_flags(diffs)
        cycle_diff = State.cycle(diffs)
        if cycle_diff:
            return 'Recycle'
        elif deck_diff:
            cards = [deck[i] for i in range(52) if is_bit_set(deck_diff, i)]
            return f"Remove {' and '.join(cards)}"
        else:
            return 'Draw'

    actions = []
    while state in seen_states:
        prev_state = seen_states[state]
        actions.append(action(prev_state, state))
        state = prev_state
    return list(reversed(actions))


def solve(deck):
    """Return a solution to removing all tableau cards in Pyramid Solitaire."""
    fringe = collections.deque()
    seen_states = dict()
    fringe.append(State.INITIAL_STATE)
    while fringe:
        state = fringe.popleft()
        if State.is_tableau_empty(state):
            return path(state, seen_states, deck)
        for next_state in State.successors(state, deck):
            if next_state not in seen_states:
                seen_states[next_state] = state
                fringe.append(next_state)
    return []
