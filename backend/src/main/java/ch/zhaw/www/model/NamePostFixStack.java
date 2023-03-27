package ch.zhaw.www.model;


import java.util.Stack;

public class NamePostFixStack {
    private final Stack<String> namePostFix = new Stack<>();

    public NamePostFixStack() {
        namePostFix.push(" 😀");
        namePostFix.push(" 😁");
        namePostFix.push(" 😂");
        namePostFix.push(" 🤣");
        namePostFix.push(" 😃");
        namePostFix.push(" 😄");
        namePostFix.push(" 😅");
        namePostFix.push(" 😆");
        namePostFix.push(" 😉");
        namePostFix.push(" 😊");
        namePostFix.push(" 😋");
        namePostFix.push(" 😎");
    }

    public String getNamePostFix() {
        return namePostFix.pop();
    }

}
