package ch.zhaw.www.model;


import java.util.Stack;
/**
 * This class contains a stack of name postfixes.
 * The postfixes are used to create unique names for players.
 */
public class NamePostfixStack {
    private final Stack<String> postFixes = new Stack<>();

    public NamePostfixStack() {
        postFixes.push(" 😀");
        postFixes.push(" 😁");
        postFixes.push(" 😂");
        postFixes.push(" 🤣");
        postFixes.push(" 😃");
        postFixes.push(" 😄");
        postFixes.push(" 😅");
        postFixes.push(" 😆");
        postFixes.push(" 😉");
        postFixes.push(" 😊");
        postFixes.push(" 😋");
        postFixes.push(" 😎");
    }

    public String popNamePostFix() {
        return postFixes.pop();
    }

}
