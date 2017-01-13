package com.koadr;


public class SagaState {
    private final int number;
    private final String word;

    public SagaState(int number, String word) {
        this.number = number;
        this.word = word;
    }

    @Override
    public String toString() {
        return "SagaState{" +
                "number=" + number +
                ", word='" + word + '\'' +
                '}';
    }

    public int getNumber() {
        return number;
    }

    public String getWord() {
        return word;
    }

    public SagaState setNumber(int number) {
        return new SagaState(number, this.word);
    }

    public SagaState setWord(String word) {
        return new SagaState(this.number, word);
    }
}
