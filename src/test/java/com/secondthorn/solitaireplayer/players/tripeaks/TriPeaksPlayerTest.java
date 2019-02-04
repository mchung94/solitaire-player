package com.secondthorn.solitaireplayer.players.tripeaks;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TriPeaksPlayerTest {
    @Test
    void cardsToString() {
        String[] args = {"Board"};
        TriPeaksPlayer player = new TriPeaksPlayer(args);
        List<String> cards = new ArrayList<>();
        for (char suit : "cdhs".toCharArray()) {
            for (char rank : "A23456789TJQK".toCharArray()) {
                cards.add("" + rank + suit);
            }
        }
        String expectedString =
                        "      Ac          2c          3c\n" +
                        "    4c  5c      6c  7c      8c  9c\n" +
                        "  Tc  Jc  Qc  Kc  Ad  2d  3d  4d  5d\n" +
                        "6d  7d  8d  9d  Td  Jd  Qd  Kd  Ah  2h\n" +
                        "3h\n" +
                        "4h 5h 6h 7h 8h 9h Th Jh Qh Kh As 2s 3s 4s 5s 6s 7s 8s 9s Ts Js Qs Ks";
        assertEquals(expectedString, player.cardsToString(cards));
    }

}
