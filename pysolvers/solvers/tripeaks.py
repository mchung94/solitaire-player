"""Find out how to 'clear the board' in TriPeaks Solitaire."""
import collections

# Cards are strings containing a rank character followed by a suit character,
# because it's simpler than defining a class or named tuple while still being
# immutable, hashable, easy to create, and human-readable.
#
# I also want to define a deck as just a tuple of cards that contain exactly
# all 52 cards in a standard deck. I think this is the simplest way with the
# fewest surprises/pitfalls.

_RANKS = 'A23456789TJQK'
_SUITS = 'cdhs'
_CARDS = [f'{rank}{suit}' for suit in _SUITS for rank in _RANKS]
_CARDS_SET = set(_CARDS)


def is_card(obj):
    """Return true if the object is a card."""
    return obj in _CARDS_SET


def card_rank(card):
    """Return the card's rank as a character."""
    return card[0]


def is_one_rank_apart(card1, card2):
    """Return true if card2 is one rank above/below card1.

    In TriPeaks, things loop around so K is next to A which is next to 2."""
    def card_value(card):
        return 'A23456789TJQK'.index(card_rank(card))

    pos1, pos2 = card_value(card1), card_value(card2)
    diff = abs(pos1 - pos2)
    return diff in (1, 12)


def malformed_cards(tuple_of_objects):
    """Return a list of the objects in the tuple that aren't cards.

    If is_standard_deck() returns false for a list, this function may help the
    caller determine what's wrong with their deck of cards."""
    return [obj for obj in tuple_of_objects if not is_card(obj)]


def missing_cards(tuple_of_cards):
    """Return a list of the standard cards that are missing from the tuple.

    Return the missing cards in consistent order by suit and rank.

    If is_standard_deck() returns false for a list, this function may help the
    caller determine what's wrong with their deck of cards."""
    cards = set(tuple_of_cards)
    return [card for card in _CARDS if card not in cards]


def duplicate_cards(tuple_of_cards):
    """Return a list of the cards that are duplicated in the tuple.

    If a card is duplicated N times, the card should be in the result N times
    so that the caller knows how many times it's been duplicated.

    If is_standard_deck() returns false for a list, this function may help the
    caller determine what's wrong with their deck of cards."""

    c = collections.Counter(tuple_of_cards)
    return [card for card in tuple_of_cards if c[card] > 1]


def is_standard_deck(tuple_of_cards):
    """Return true if the tuple of cards is a standard 52-card deck."""
    if not isinstance(tuple_of_cards, tuple):
        return False
    return len(tuple_of_cards) == 52 and not missing_cards(tuple_of_cards)


class State(collections.namedtuple('State', 'tableau stock_pile waste_card')):
    """A state in a TriPeaks Solitaire state game.

    States are named tuples consisting of immutable things so that they can be
    keys in a dictionary.

    The tableau is a tuple of 28 cards or None when the card is removed.
    The stock_pile is a tuple of up to 23 cards representing the stock pile.
    The waste_card is the card at the top of the waste pile.

    The tableau cards are indexed like this:
           0           1           2
         3   4       5   6       7   8
       9  10  11  12  13  14  15  16  17
    18  19  20  21  22  23  24  25  26  27

    The first card in the stock_pile is the top of the stock pile."""

    # the tableau indexes directly under each card
    CHILD_INDEXES = [
        (3, 4),
        (5, 6),
        (7, 8),
        (9, 10),
        (10, 11),
        (12, 13),
        (13, 14),
        (15, 16),
        (16, 17),
        (18, 19),
        (19, 20),
        (20, 21),
        (21, 22),
        (22, 23),
        (23, 24),
        (24, 25),
        (25, 26),
        (26, 27),
        (),
        (),
        (),
        (),
        (),
        (),
        (),
        (),
        (),
        (),
    ]

    @staticmethod
    def initial_state(deck):
        """Create the initial state of the game from a deck of cards.

        The cards at index 0-27 are the initial tableau, the card at index 28
        is the start of the waste pile, and the cards at index 29-51 are the
        stock pile.  The top of the stock pile is the card at index 29."""
        if not is_standard_deck(deck):
            raise ValueError(f'Not a standard deck of cards: {deck}')
        return State(tuple(deck[:28]), tuple(deck[29:]), deck[28])

    def is_face_up(self, tableau_index):
        """Return true if the tableau card is face up.

        Face up cards on the tableau are the cards that haven't been removed
        yet and aren't covered from below by other cards. They can be moved to
        the waste pile if they are one rank above or below the waste card."""
        if self.tableau[tableau_index] is None:
            return False
        child_indexes = State.CHILD_INDEXES[tableau_index]
        return all((self.tableau[i] is None for i in child_indexes))

    def can_be_moved(self, card):
        """Return true if the card can be moved to the waste pile.

        A card can be moved to the waste pile if its rank is one above/below
        the rank of the card at the top of the waste pile. It wraps around so
        that Aces"""
        return is_one_rank_apart(self.waste_card, card)

    def is_tableau_empty(self):
        """Return true if there are no more tableau cards remaining."""
        return all(self.tableau[i] is None for i in range(28))

    def successors(self):
        """Return a list of successor states to this state."""
        next_states = []
        if self.stock_pile:
            tableau = self.tableau
            stock = self.stock_pile[1:]
            waste = self.stock_pile[0]
            next_states.append(State(tableau, stock, waste))
        for i, card in enumerate(self.tableau):
            if self.is_face_up(i) and self.can_be_moved(card):
                tableau = tuple(c if c != card else None for c in self.tableau)
                stock = self.stock_pile
                waste = card
                next_states.append(State(tableau, stock, waste))
        return next_states


def path(current_state, seen_states):
    """Return the cards to move to the waste to reach the current state."""
    cards = []
    while current_state in seen_states:
        cards.append(current_state.waste_card)
        current_state = seen_states[current_state]
    return list(reversed(cards))


def solve(deck):
    """Return a solution to removing all tableau cards in TriPeaks Solitaire.

    This returns a list of cards to move to the waste pile in order to solve
    the game. It will be the shortest possible solution. If there is no
    solution, an empty list will be returned."""
    fringe = collections.deque()
    seen_states = dict()
    fringe.append(State.initial_state(deck))
    while fringe:
        state = fringe.popleft()
        if state.is_tableau_empty():
            return path(state, seen_states)
        for next_state in state.successors():
            if next_state not in seen_states:
                seen_states[next_state] = state
                fringe.append(next_state)
    return []