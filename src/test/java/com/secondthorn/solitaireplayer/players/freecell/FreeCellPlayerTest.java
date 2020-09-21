package com.secondthorn.solitaireplayer.players.freecell;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FreeCellPlayerTest {
    static final String DECK_STRING = "9d 9h Ah Td 4h Kd 7c 3d " +
                               "5s 8s Ac 2c Jh 4s 6c As " +
                               "6s 8d 8h Tc Qh 7s Kc 3h " +
                               "Ts 3c Ks 2h Qd 5c Jc Kh " +
                               "7d 4c 9c 2s Th Qc Js 5h " +
                               "7h Jd 6h 2d 6d 4d 3s Ad " +
                               "8c 5d 9s Qs";

    static final List<String> cards;

    static {
        cards = new ArrayList<>();
        Collections.addAll(cards, DECK_STRING.split("\\s"));
    }

    @Test
    void cardsToString() {
        String[] args = {};
        FreeCellPlayer player = new FreeCellPlayer(args);
        String expected = "9d 9h Ah Td 4h Kd 7c 3d\n" +
                "5s 8s Ac 2c Jh 4s 6c As\n" +
                "6s 8d 8h Tc Qh 7s Kc 3h\n" +
                "Ts 3c Ks 2h Qd 5c Jc Kh\n" +
                "7d 4c 9c 2s Th Qc Js 5h\n" +
                "7h Jd 6h 2d 6d 4d 3s Ad\n" +
                "8c 5d 9s Qs";
        String actual = player.cardsToString(cards);
        assertEquals(expected, actual);
    }

    @Test
    void cardsToFCSolveString() {
        String[] args = {};
        FreeCellPlayer player = new FreeCellPlayer(args);
        String expected = "9D 5S 6S TS 7D 7H 8C\n" +
                          "9H 8S 8D 3C 4C JD 5D\n" +
                          "AH AC 8H KS 9C 6H 9S\n" +
                          "TD 2C TC 2H 2S 2D QS\n" +
                          "4H JH QH QD TH 6D\n" +
                          "KD 4S 7S 5C QC 4D\n" +
                          "7C 6C KC JC JS 3S\n" +
                          "3D AS 3H KH 5H AD";
        String actual = player.cardsToFCSolveString(cards);
        assertEquals(expected, actual);
    }
}
