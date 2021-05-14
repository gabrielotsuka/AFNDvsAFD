package com.br.gabrielotsuka.service;

import com.br.gabrielotsuka.data.AFNDAutomaton;
import com.br.gabrielotsuka.data.AFNDRule;

import java.util.ArrayList;
import java.util.List;

public class AFNDService {


    private final AFNDRuleService afndRuleService = new AFNDRuleService();
    private boolean sequenceValidate = false;

    public void belongsToLanguage(String sequence, AFNDAutomaton automaton) throws Exception {
        List<Character> sequenceRequest = new ArrayList<>();

        for (char item: sequence.toCharArray()) {
            sequenceRequest.add(item);
        }

        if(sequenceRequest.isEmpty()){
            List<String> initialState = new ArrayList<>();
            initialState.add(automaton.getInitialState());
            sequenceValidate = isAcceptable(initialState, automaton.getFinalStates());
        }else{
            processSequence(sequenceRequest,
                    automaton.getInitialState(),
                    automaton.getRules(),
                    automaton.getFinalStates());
        }
    }

    public List<AFNDRule> getStackSequence(){
        return afndRuleService.ruleRepository.coveredRules;
    }

    public Boolean getSequenceValidate(){
        return sequenceValidate;
    }

    private void processSequence(List<Character> sequence,
                                String currentState,
                                List<AFNDRule> rules,
                                List<String> finalStates) throws Exception {

        int aux = 0;

        for (char currentSymbol : sequence) {

            aux = aux + 1;

            AFNDRule applicableRule = afndRuleService.getApplicableRule(rules, currentState, currentSymbol);

            afndRuleService.addCoveredRule(applicableRule);

            if(applicableRule.getTargetStates().size() > 1) {
                for (String targetStates : applicableRule.getTargetStates()) {
                    if(sequence.subList(aux, sequence.size()).size() != 0){
                        processSequence(
                                sequence.subList(aux, sequence.size()),
                                targetStates,
                                rules,
                                finalStates); //recursão
                    }
                }
            }else{
                if(applicableRule.getTargetStates().size() == 1) {
                    currentState = applicableRule.getTargetStates().get(0);
                }else if (applicableRule.getTargetStates().isEmpty()){
                    break;
                }
            }
        }
        isAcceptableState(finalStates);
    }

    private void isAcceptableState(List<String> finalStates){
        if(!sequenceValidate)
            sequenceValidate = isAcceptable(getStackSequence().get(getStackSequence().size()-1).getTargetStates(), finalStates);
    }

    private boolean isAcceptable(List<String> state, List<String> acceptableStates) {
        for (String item: state) {
            return acceptableStates.contains(item);
        }
        return false;
    }


    public void validateAutomaton(AFNDAutomaton automaton) throws Exception {
        List<String> states = automaton.getStates();
        List<String> finalStates = automaton.getFinalStates();
        String initialState = automaton.getInitialState();
        String alphabet = automaton.getAlphabet();
        List<AFNDRule> rules = automaton.getRules();

        validateStates(states);
        validateInitialStates(states, initialState);
        validateFinalStates(states, finalStates);
        validateRules(rules, states, alphabet);
    }

    private void validateStates(List<String> states) throws Exception {
        if (states.isEmpty())
            throw new Exception("Conjunto de estados deve possuir pelo menos um estado");
    }

    private void validateInitialStates(List<String> states, String initialState) throws Exception {
        if(!states.contains(initialState))
            throw new Exception("Estado inicial deve pertencer ao conjunto de estados!");
    }

    private void validateFinalStates(List<String> states, List<String> finalStates) throws Exception {
        if (finalStates.isEmpty())
            throw new Exception("Deve haver pelo menos um estado final");
        for (String finalState : finalStates) {
            if (!states.contains(finalState))
                throw new Exception("Estados finais devem pertencer ao conjunto de estados!");
        }
    }

    private void validateRules(List<AFNDRule> rules, List<String> states, String alphabet) throws Exception {
        for (AFNDRule rule: rules) {
            for (String targetState: rule.getTargetStates()) {
               if (!states.contains(targetState))
                   throw new Exception("Estados das regras devem pertencer ao conjunto de estados!");
            }
            if(!states.contains(rule.getSourceState()))
                throw new Exception("Estados das regras devem pertencer ao conjunto de estados!");
            if(alphabet.indexOf(rule.getSymbol()) == -1)
                throw new Exception("Símbolos das regras devem pertencer ao alfabeto!");
        }
    }
}
