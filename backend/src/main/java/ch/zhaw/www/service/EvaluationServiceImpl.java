package ch.zhaw.www.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EvaluationServiceImpl {
    Map<String, List<String>> selections = new HashMap<>();
    List<Proposition> propositions = List.of(
            new Proposition("ID-0001", "PlayerId_A", List.of("Oranges", "bananas"), false),
            new Proposition("ID-0002", "PlayerId_B", List.of("Apples", "Oranges"), false),
            new Proposition("ID-0003", "PlayerId_C", List.of("Yoghurt", "bananas"), false),
            new Proposition("ID-0004", "PlayerId_D", List.of("Pancakes", "bananas"), false),
            new Proposition("ID-0005", "PlayerId_E", List.of("Apples", "bananas"), true)
    );

    @Getter
    @RequiredArgsConstructor
    static class Proposition {
        private final String proposition_Id;
        private final String playerId;
        private final List<String> gaps;
        private final boolean isSphinxProposition;
    }

    public void submitSelections() {
        // the ID is the key of the map and represents the proposition
        // the list of playerIDs is the value of the map and represents the players who selected the proposition
        selections.put(propositions.get(0).proposition_Id, List.of(propositions.get(1).playerId, propositions.get(2).playerId));
        selections.put(propositions.get(1).proposition_Id, List.of(propositions.get(2).playerId, propositions.get(3).playerId));
        selections.put(propositions.get(2).proposition_Id, List.of(propositions.get(3).playerId, propositions.get(4).playerId));
        selections.put(propositions.get(3).proposition_Id, List.of(propositions.get(4).playerId, propositions.get(0).playerId));

    }

    public Map<String, Integer> evaluateSelections() throws RoundError.IllegalStateException {
        Map<String, Integer> evaluationMap = new HashMap<>();

        // iterate over the entries in the selections map
        for (Map.Entry<String, List<String>> entry : selections.entrySet()) {
            // get the proposition ID and the list of player IDs
            String propositionId = entry.getKey();
            List<String> playerIds = entry.getValue();
            // compute the number of players who selected the proposition
            int numPlayers = playerIds.size();
            // add the proposition and the number of players to the evaluation map
            evaluationMap.put(propositionId, numPlayers);
        }
        return evaluationMap;
    }

    // print the evaluation map
    public void printEvaluationMap(Map<String, Integer> evaluationMap) {
        for (Map.Entry<String, Integer> entry : evaluationMap.entrySet()) {
            String propositionId = entry.getKey();
            int numPlayers = entry.getValue();
            System.out.println("Proposition ID: " + propositionId + " has " + numPlayers + " selections");
        }
    }

    public static void main(String[] args) {
        EvaluationServiceImpl evaluationService = new EvaluationServiceImpl();
        evaluationService.submitSelections();
        Map<String, Integer> evaluationMap = evaluationService.evaluateSelections();
        evaluationService.printEvaluationMap(evaluationMap);
    }

}