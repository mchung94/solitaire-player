package com.secondthorn.solitaireplayer.players.pyramid;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PyramidPlayerTest {
    @Test
    public void pyramidString() {
        String[] args = {"Board"};
        PyramidPlayer player = new PyramidPlayer(args);
        List<String> cards = new ArrayList<>();
        for (char suit : "cdhs".toCharArray()) {
            for (char rank : "A23456789TJQK".toCharArray()) {
                cards.add("" + rank + suit);
            }
        }
        String expectedString =
                "            Ac\n" +
                "          2c  3c\n" +
                "        4c  5c  6c\n" +
                "      7c  8c  9c  Tc\n" +
                "    Jc  Qc  Kc  Ad  2d\n" +
                "  3d  4d  5d  6d  7d  8d\n" +
                "9d  Td  Jd  Qd  Kd  Ah  2h\n" +
                "3h 4h 5h 6h 7h 8h 9h Th Jh Qh Kh As 2s 3s 4s 5s 6s 7s 8s 9s Ts Js Qs Ks";
        assertEquals(expectedString, player.pyramidString(cards));
    }
}
