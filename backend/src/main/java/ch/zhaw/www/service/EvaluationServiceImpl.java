package ch.zhaw.www.service;

import ch.zhaw.www.model.Proposition;
import ch.zhaw.www.model.Round;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EvaluationServiceImpl implements EvaluationService {
    private static final int ZERO = 0;
    private static final int SINGLE_POINT = 1;
    
    /**
     * Rules for point distribution: - If the selected proposition is the sphinx's, the selector gets 1 point. The
     * sphinx gets a point as well but only if at least one non-sphinx proposition has been selected in this round. - If
     * the selected proposition is not the sphinx's, the selector gets no point and the submitter of the proposition
     * gets 1 point if they are the only submitter. - The sphinx is not allowed to select any proposition. - Players are
     * only allowed to select their own proposition if they are not the only submitter of that proposition.
     */
    @Override
    public Map<String, Integer> evaluateSelection(Round round, String selectedPropositionId, String selectorId) {
        Map<String, Integer> evaluation = new HashMap<>();
        String sphinxId = Objects.requireNonNull(round.getSphinx()).getId();
        
        Optional<Proposition> optionalProposition = findProposition(round, selectedPropositionId);
        if (optionalProposition.isEmpty() || isIllegalSelection(optionalProposition.get(), sphinxId, selectorId)) {
            return evaluation;
        }
        
        Proposition proposition = optionalProposition.get();
        List<String> submitterIds = proposition.getPlayerIds();
        
        if (submitterIds.contains(sphinxId)) {
            evaluation.put(selectorId, SINGLE_POINT);
        } else {
            round.setHasNonSphinxPropositionBeenSelected(true);
        }
        
        if (submitterIds.size() == 1) {
            String submitterId = submitterIds.get(0);
            if (submitterId.equals(sphinxId)) {
                round.setTempSphinxPoints(round.getTempSphinxPoints() + SINGLE_POINT);
            } else {
                evaluation.put(submitterId, SINGLE_POINT);
            }
        }
        
        if (round.isHasNonSphinxPropositionBeenSelected()) {
            evaluation.put(sphinxId, round.getTempSphinxPoints());
            round.setTempSphinxPoints(ZERO);
        }
        return evaluation;
    }
    
    private static Optional<Proposition> findProposition(Round round, String selectedPropositionId) {
        return round.getPropositions()
                .stream()
                .filter(proposition -> selectedPropositionId.equals(proposition.getId()))
                .findFirst();
    }
    
    private boolean isIllegalSelection(Proposition proposition, String sphinxId, String selectorId) {
        return selectorId.equals(sphinxId) || submitterIsOnlyProposer(proposition, selectorId);
    }
    
    private static boolean submitterIsOnlyProposer(Proposition proposition, String selectorId) {
        return !proposition.hasDuplicates() && proposition.hasSubmitter(selectorId);
    }
}