package ch.zhaw.www.model;
import lombok.Data;

import java.util.List;

/**
 * This class represents a Turn, which consists of a set of propositions and a set of selections
 * made by the players. The turn also contains the sphinx, who will create a proposition but not select one.
 */
@Data
public class Turn {

    // TODO: reflect on what the Turn class should contain and how it interacts with the other classes then implement it

    private static final int MAX_ROUNDS = 5;
    Player sphinx;
    List<Player> players;
    Round[] rounds = new Round[MAX_ROUNDS];

}
