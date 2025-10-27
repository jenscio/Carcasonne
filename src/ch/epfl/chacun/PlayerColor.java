package ch.epfl.chacun;

import java.util.List;

/**
 * PlayerColor
 * Enumeration qui représente toutes les couleurs que les joueurs peuvent avoir
 *
 * @author Rayan Berrada (378940)
 * @author Jens Cancio (379510)
 */

public enum PlayerColor {
    RED,
    BLUE,
    GREEN,
    YELLOW,
    PURPLE;

    public final static PlayerColor[] PlayerColorArray = new PlayerColor[] {
            PlayerColor.RED, PlayerColor.BLUE, PlayerColor.GREEN, PlayerColor.YELLOW, PlayerColor.PURPLE
    };
    public final static List<PlayerColor> ALL = List.of(PlayerColorArray);

    }






