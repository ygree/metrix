package com.koadr;


public class SagaState {
    private int number = Integer.MAX_VALUE;
    private String word = "";
    private int number2 = Integer.MAX_VALUE;
    private String word2 = "";
    private int number3 = Integer.MAX_VALUE;

    public boolean isCompleteStep1() {
        return number < Integer.MAX_VALUE && !word.isEmpty();
    }

    public boolean isCompleteStep2() {
        return number2 < Integer.MAX_VALUE && !word2.isEmpty();
    }

    public boolean isCompleteStep3() {
        return number3 < Integer.MAX_VALUE;
    }

    public int getNumber() {
        return number;
    }

    public String getWord() {
        return word;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setNumber2(int number) {
        this.number2 = number;
    }

    public void setNumber3(int number) {
        this.number3 = number;
    }

    public void setWord2(String word) {
        this.word2 = word;
    }

    @Override
    public String toString() {
        return "SagaState{" +
                "number=" + number +
                ", word='" + word + '\'' +
                ", number2=" + number2 +
                ", word2='" + word2 + '\'' +
                ", number3=" + number3 +
                '}';
    }
}
