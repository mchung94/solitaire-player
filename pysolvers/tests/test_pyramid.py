import unittest

import solvers.pyramid as py

from tests.test_deck import ALL_CARDS


class TestPyramidCardFunctions(unittest.TestCase):
    @staticmethod
    def card_value(card):
        return {
            'A': 1,
            '2': 2,
            '3': 3,
            '4': 4,
            '5': 5,
            '6': 6,
            '7': 7,
            '8': 8,
            '9': 9,
            'T': 10,
            'J': 11,
            'Q': 12,
            'K': 13,
        }[card[0]]

    def test_card_numeric_value(self):
        for card in ALL_CARDS:
            self.assertEqual(
                TestPyramidCardFunctions.card_value(card),
                py.card_value(card)
            )

    def test_cards_are_removable(self):
        for card in ALL_CARDS:
            self.assertEqual(
                card[0] == 'K',
                py.cards_are_removable(card)
            )

        for card1 in ALL_CARDS:
            value1 = TestPyramidCardFunctions.card_value(card1)
            for card2 in ALL_CARDS:
                value2 = TestPyramidCardFunctions.card_value(card2)
                self.assertEqual(
                    value1 + value2 == 13,
                    py.cards_are_removable(card1, card2)
                )


class TestState(unittest.TestCase):
    @staticmethod
    def make_state(deck, stock_index, cycle, *cards_to_remove):
        """Create a state with the given fields and cards removed."""
        deck_flags = 0xFFFFFFFFFFFFF
        for card in cards_to_remove:
            index = ALL_CARDS.index(card)
            deck_flags ^= (1 << index)
        return (cycle << 58) | (stock_index << 52) | deck_flags

    def test_initial_state(self):
        self.assertEqual(0x1CFFFFFFFFFFFFF, py.State.INITIAL_STATE)

    def test_deck_flags(self):
        state = py.State.INITIAL_STATE
        self.assertEqual(0xFFFFFFFFFFFFF, py.State.deck_flags(state))

    def test_stock_index(self):
        self.assertEqual(28, py.State.stock_index(py.State.INITIAL_STATE))

    def test_cycle(self):
        self.assertEqual(0, py.State.cycle(py.State.INITIAL_STATE))

    def test_waste_index(self):
        self.assertEqual(27, py.State.waste_index(py.State.INITIAL_STATE))
        state = TestState.make_state(ALL_CARDS, 52, 0)
        self.assertEqual(51, py.State.waste_index(state))
        state = TestState.make_state(ALL_CARDS, 52, 0, 'Ts', 'Js', 'Qs', 'Ks')
        self.assertEqual(47, py.State.waste_index(state))

    def check_successors(self, deck, state, *expected_successors):
        """Test that the state's successors match the expected successors."""
        actual = py.State.successors(state, deck)
        self.assertEqual(set(expected_successors), set(actual))

    def test_successors_draw(self):
        self.check_successors(
            ALL_CARDS, TestState.make_state(ALL_CARDS, 28, 0),
            TestState.make_state(ALL_CARDS, 29, 0),
            TestState.make_state(ALL_CARDS, 28, 0, 'Jd', '2h'),
            TestState.make_state(ALL_CARDS, 28, 0, 'Qd', 'Ah'),
            TestState.make_state(ALL_CARDS, 28, 0, 'Kd'),
            TestState.make_state(ALL_CARDS, 29, 0, 'Td', '3h'),
        )

    def test_successors_recycle(self):
        self.check_successors(
            ALL_CARDS, TestState.make_state(ALL_CARDS, 52, 0),
            TestState.make_state(ALL_CARDS, 28, 1),
            TestState.make_state(ALL_CARDS, 52, 0, 'Jd', '2h'),
            TestState.make_state(ALL_CARDS, 52, 0, 'Qd', 'Ah'),
            TestState.make_state(ALL_CARDS, 52, 0, 'Kd'),
            TestState.make_state(ALL_CARDS, 52, 0, 'Ks'),
        )

    def test_successors_end_of_last_cycle(self):
        self.check_successors(
            ALL_CARDS, TestState.make_state(ALL_CARDS, 52, 2),
            TestState.make_state(ALL_CARDS, 52, 2, 'Jd', '2h'),
            TestState.make_state(ALL_CARDS, 52, 2, 'Qd', 'Ah'),
            TestState.make_state(ALL_CARDS, 52, 2, 'Kd'),
            TestState.make_state(ALL_CARDS, 52, 2, 'Ks'),
        )

    def test_successors_remove_king_from_stock(self):
        self.check_successors(
            ALL_CARDS, TestState.make_state(ALL_CARDS, 51, 0),
            TestState.make_state(ALL_CARDS, 52, 0),
            TestState.make_state(ALL_CARDS, 51, 0, 'Jd', '2h'),
            TestState.make_state(ALL_CARDS, 51, 0, 'Qd', 'Ah'),
            TestState.make_state(ALL_CARDS, 51, 0, 'Kd'),
            TestState.make_state(ALL_CARDS, 51, 0, 'Ah', 'Qs'),
            TestState.make_state(ALL_CARDS, 52, 0, 'Ks'),
        )

    def test_successors_stock_and_waste_pair(self):
        self.check_successors(
            ALL_CARDS, TestState.make_state(ALL_CARDS, 32, 0),
            TestState.make_state(ALL_CARDS, 33, 0, '6h', '7h'),
            TestState.make_state(ALL_CARDS, 33, 0),
            TestState.make_state(ALL_CARDS, 32, 0, 'Jd', '2h'),
            TestState.make_state(ALL_CARDS, 32, 0, 'Qd', 'Ah'),
            TestState.make_state(ALL_CARDS, 32, 0, 'Kd'),
        )


@unittest.skip('Slow Pyramid Solitaire solver tests')
class TestSolver(unittest.TestCase):
    def test_minimal(self):
        deck = '''
                    Kd
                  Kc  Qh
                Ah  7d  6d
              8d  5d  9d  4d
            Td  3d  Jd  2d  Qd
          Ad  7c  6c  8c  5c  9c
        4c  Tc  3c  Jc  2c  Qc  Ac
        6h 7h 5h 8h 4h 9h 3h Th 2h Jh Kh As 2s 3s 4s 5s 6s 7s 8s 9s Ts Js Qs Ks
        '''.split()
        solution = py.solve(deck)
        self.assertEqual(15, len(solution))

    def test_solver(self):
        solution = py.solve(ALL_CARDS)
        self.assertEqual(27, len(solution))

    def test_impossible_game_has_no_solution(self):
        # this takes 48 minutes on my machine, optimized should be 60x faster
        deck = '''
                    Th
                  2h  4d
                3h  Qd  8h
              9h  5d  Jc  Td
            7c  4c  Ts  Ac  9c
          8d  5s  2s  7h  6s  7s
        2c  9d  Qs  3d  5c  5h  Ad
        8s Js 6c 9s 4h Kh Jd 4s 2d 6d Ks Qc 3s 3c Kc 7d Tc Ah 6h Qh Kd 8c As Jh
        '''.split()
        solution = py.solve(deck)
        self.assertEqual([], solution)
