package ch.zhaw.www.model;


import java.util.Stack;
/**
 * This class represents a stack of name postfixes.
 * The postfixes are used to create unique names for players.
 */
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

    public String popNamePostFix() {
        return namePostFix.pop();
    }

}
