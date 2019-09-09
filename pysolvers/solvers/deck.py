"""Card and Deck definitions.

Cards are strings containing a rank character followed by a suit character,
because it's simpler than defining a class or named tuple while still being
immutable, hashable, easy to create, and human-readable.

I also want to define a deck as just a tuple of cards that contain exactly
all 52 cards in a standard deck. I think this is the simplest way with the
fewest surprises/pitfalls.
"""
import collections

RANKS = 'A23456789TJQK'
SUITS = 'cdhs'
CARDS = [f'{rank}{suit}' for suit in SUITS for rank in RANKS]
CARDS_SET = set(CARDS)


def is_card(obj):
    """Return true if the object is a card."""
    return obj in CARDS_SET


def card_rank(card):
    """Return the card's rank as a character."""
    return card[0]


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
    return [card for card in CARDS if card not in cards]


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


