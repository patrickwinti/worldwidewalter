package ch.zhaw.www.model;


import java.util.Stack;

/**
 * This class contains a stack of name postfixes.
 * The postfixes are used to create unique names for players.
 * Note that this stack only contains 12 postfixes, so if more than 12 players of the same name join the game,
 * (which currently is not possible) the Stack will be empty and will throw an EmptyStackException.
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
