import unittest

import solvers.deck


ALL_CARDS = (
    'Ac', '2c', '3c', '4c', '5c', '6c', '7c', '8c', '9c', 'Tc', 'Jc', 'Qc', 'Kc',
    'Ad', '2d', '3d', '4d', '5d', '6d', '7d', '8d', '9d', 'Td', 'Jd', 'Qd', 'Kd',
    'Ah', '2h', '3h', '4h', '5h', '6h', '7h', '8h', '9h', 'Th', 'Jh', 'Qh', 'Kh',
    'As', '2s', '3s', '4s', '5s', '6s', '7s', '8s', '9s', 'Ts', 'Js', 'Qs', 'Ks'
)


class TestCardAndDeck(unittest.TestCase):
    def test_card_ranks(self):
        for card in ALL_CARDS:
            self.assertEqual(card[0], solvers.deck.card_rank(card))

    def test_missing_cards(self):
        self.assertEqual(0, len(solvers.deck.missing_cards(ALL_CARDS)))
        self.assertEqual(['Ac'], solvers.deck.missing_cards(ALL_CARDS[1:]))

    def test_duplicate_cards(self):
        self.assertEqual(0, len(solvers.deck.duplicate_cards(ALL_CARDS)))
        self.assertEqual(['Ac', 'Ac'], solvers.deck.duplicate_cards(ALL_CARDS + ('Ac',)))
        self.assertEqual(['Ac', 'Ac', 'Ac'], solvers.deck.duplicate_cards(ALL_CARDS + ('Ac', 'Ac')))

    def test_malformed_cards(self):
        self.assertEqual(0, len(solvers.deck.malformed_cards(ALL_CARDS)))
        input = ['7S', 'ks', 'KS', 'kS', None, '', 0, 'Ks']
        expected = ['7S', 'ks', 'KS', 'kS', None, '', 0]
        self.assertEqual(expected, solvers.deck.malformed_cards(input))

    def test_is_standard_deck(self):
        self.assertTrue(solvers.deck.is_standard_deck(ALL_CARDS))
        self.assertFalse(solvers.deck.is_standard_deck(ALL_CARDS[1:]))
        self.assertFalse(solvers.deck.is_standard_deck([]))
