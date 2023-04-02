package ch.zhaw.www.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class EvaluationServiceImpl {
    Map<String, List<String>> selections = new HashMap<>();
    public void submitSelections() {
        // the ID is the key of the map and represents the proposition
        // the list of playerIDs is the value of the map and represents the players who selected the proposition
        selections.put("ID-0001",List.of("PlayerID A","PlayerID B","PlayerID C"));
        selections.put("ID-0002",List.of("PlayerID D","PlayerID E"));
    }

    public Map<String, Integer> evaluateRound() throws RoundError.IllegalStateException {
        Map<String, Integer> evaluationMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : selections.entrySet()) {
            String propositionId = entry.getKey();
            List<String> playerIds = entry.getValue();
            int numPlayers = playerIds.size();
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
        Map<String, Integer> evaluationMap = evaluationService.evaluateRound();
        evaluationService.printEvaluationMap(evaluationMap);
    }

}