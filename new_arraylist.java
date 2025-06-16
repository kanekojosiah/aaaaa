import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Random;

class MathProblemGeneratorGUI_arraylist extends JFrame {
    private final JSpinner operandDigitsSpinner = new JSpinner(new SpinnerNumberModel(2, 1, Integer.MAX_VALUE, 1));
    private final JSpinner questionsSpinner = new JSpinner(new SpinnerNumberModel(5, 1, Integer.MAX_VALUE, 1));
    private final JSpinner operandCountSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 10, 1));
    private final JCheckBox additionCheck = new JCheckBox("Addition (+)");
    private final JCheckBox subtractionCheck = new JCheckBox("Subtraction (-)");
    private final JCheckBox multiplicationCheck = new JCheckBox("Multiplication (×)");
    private final JCheckBox divisionCheck = new JCheckBox("Division (÷)");
    private final JCheckBox mixedCheck = new JCheckBox("Mixed (Random)");
    private final JTextField answerField = new JTextField(10);
    private final JLabel questionLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel progressLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JButton nextButton = new JButton("Next Question");
    private final JButton generateButton = new JButton("Start New Quiz");

    private final ArrayList<Problem> problems = new ArrayList<>();
    private final ArrayList<String> userInputs = new ArrayList<>();
    private DefaultListModel<String> questionListModel = new DefaultListModel<>();
    private JList<String> questionList;
    private int currentIndex = 0;
    private int score = 0;

    public MathProblemGeneratorGUI_arraylist() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Math Problem Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Left Panel for Question List
        JPanel leftPanel = new JPanel(new BorderLayout());
        questionList = new JList<>(questionListModel);
        questionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                currentIndex = questionList.getSelectedIndex();
                if (currentIndex >= 0 && currentIndex < problems.size()) {
                    showSelectedQuestion();
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(questionList);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        // Control Panel (Top)
        JPanel controlPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.add(new JLabel("Number of Digits per operand (min 1):"));
        controlPanel.add(operandDigitsSpinner);
        controlPanel.add(new JLabel("Number of Questions (min 1):"));
        controlPanel.add(questionsSpinner);
        controlPanel.add(new JLabel("Number of Operands (min 2):"));
        controlPanel.add(operandCountSpinner);

        // Operations Panel (Checkboxes)
        JPanel operationsPanel = new JPanel();
        operationsPanel.setBorder(BorderFactory.createTitledBorder("Operations"));
        operationsPanel.setLayout(new BoxLayout(operationsPanel, BoxLayout.Y_AXIS));
        operationsPanel.add(additionCheck);
        operationsPanel.add(subtractionCheck);
        operationsPanel.add(multiplicationCheck);
        operationsPanel.add(divisionCheck);
        operationsPanel.add(mixedCheck);
        mixedCheck.addActionListener(e -> {
            boolean isMixed = mixedCheck.isSelected();
            additionCheck.setEnabled(!isMixed);
            subtractionCheck.setEnabled(!isMixed);
            multiplicationCheck.setEnabled(!isMixed);
            divisionCheck.setEnabled(!isMixed);
        });

        // Question Panel (Center)
        JPanel questionPanel = new JPanel(new BorderLayout(10, 10));
        questionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        questionPanel.add(progressLabel, BorderLayout.NORTH);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 24));
        questionPanel.add(questionLabel, BorderLayout.CENTER);
        feedbackLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        questionPanel.add(feedbackLabel, BorderLayout.SOUTH);

        // Combine operations and question panels into one center panel
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(operationsPanel, BorderLayout.NORTH);
        centerPanel.add(questionPanel, BorderLayout.CENTER);

        // Answer Panel (Bottom)
        JPanel answerPanel = new JPanel();
        answerPanel.add(new JLabel("Your answer: "));
        answerPanel.add(answerField);
        nextButton.addActionListener(this::nextQuestion);
        nextButton.setEnabled(false);
        answerPanel.add(nextButton);

        // Generate Button
        generateButton.addActionListener(this::generateProblems);

        // Add all to frame
        add(controlPanel, BorderLayout.NORTH);
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(generateButton, BorderLayout.EAST);
        add(answerPanel, BorderLayout.SOUTH);

        setSize(650, 400);
        setLocationRelativeTo(null);
    }

    private void generateProblems(ActionEvent e) {
        if (!(additionCheck.isSelected() || subtractionCheck.isSelected()
                || multiplicationCheck.isSelected() || divisionCheck.isSelected() || mixedCheck.isSelected())) {
            JOptionPane.showMessageDialog(this, "Please select at least one operation!");
            return;
        }

        problems.clear();
        userInputs.clear();
        int needed = (int) questionsSpinner.getValue();
        Random rnd = new Random();
        int digits = (int) operandDigitsSpinner.getValue();
        int operandCount = (int) operandCountSpinner.getValue();

        int minOperandValue = (int) Math.pow(10, digits - 1);
        int maxOperandValue = (int) Math.pow(10, digits) - 1;
        if (digits == 1) {
            minOperandValue = 0;
        }

        questionListModel.clear();
        while (problems.size() < needed) {
            String op;
            if (mixedCheck.isSelected()) {
                op = getRandomOperation(true, true, true, true, rnd);
            } else {
                op = getRandomOperation(
                        additionCheck.isSelected(),
                        subtractionCheck.isSelected(),
                        multiplicationCheck.isSelected(),
                        divisionCheck.isSelected(),
                        rnd
                );
            }

            ArrayList<Integer> operands = new ArrayList<>();
            for (int i = 0; i < operandCount; i++) {
                operands.add(rand(minOperandValue, maxOperandValue, rnd));
            }

            int result = operands.get(0);
            StringBuilder questionSb = new StringBuilder();
            questionSb.append(operands.get(0));

            for (int i = 1; i < operands.size(); i++) {
                int nextVal = operands.get(i);
                switch (op) {
                    case "+":
                        result += nextVal;
                        questionSb.append(" + ").append(nextVal);
                        break;
                    case "-":
                        result -= nextVal;
                        questionSb.append(" - ").append(nextVal);
                        break;
                    case "×":
                        result *= nextVal;
                        questionSb.append(" × ").append(nextVal);
                        break;
                    case "÷":
                        if (nextVal == 0) continue;
                        // Ensure division results in integer
                        if (result % nextVal != 0) {
                            result = nextVal * (result / nextVal);
                        }
                        result /= nextVal;
                        questionSb.append(" ÷ ").append(nextVal);
                        break;
                }
            }

            String questionString = questionSb.toString();
            problems.add(new Problem(questionString, result));
            questionListModel.addElement(questionString + " =");
            userInputs.add("");
        }

        currentIndex = 0;
        questionList.setSelectedIndex(0);
        nextButton.setEnabled(true);
        showSelectedQuestion();
    }

    private void showSelectedQuestion() {
        if (currentIndex >= 0 && currentIndex < problems.size()) {
            Problem p = problems.get(currentIndex);
            progressLabel.setText("Question " + (currentIndex + 1) + " / " + problems.size());
            questionLabel.setText(p.getQuestionString() + " = ?");
            feedbackLabel.setText(" ");
            answerField.setText(userInputs.get(currentIndex));
            answerField.requestFocus();
        }
    }

    private void nextQuestion(ActionEvent e) {
        if (currentIndex >= 0 && currentIndex < problems.size()) {
            Problem current = problems.get(currentIndex);
            String input = answerField.getText().trim();
            userInputs.set(currentIndex, input);

            String fb;
            try {
                int userAns = Integer.parseInt(input);
                if (userAns == current.getAnswer()) {
                    score++;
                    fb = "Correct! Ans: " + current.getAnswer();
                } else {
                    fb = "Incorrect. Ans: " + current.getAnswer();
                }
            } catch (NumberFormatException ex) {
                fb = "Invalid input. Ans: " + current.getAnswer();
            }
            feedbackLabel.setText(fb);

            if (currentIndex < problems.size() - 1) {
                currentIndex++;
                questionList.setSelectedIndex(currentIndex);
                showSelectedQuestion();
            } else {
                showResults();
                nextButton.setEnabled(false);
            }
        }
    }

    private void showResults() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Final Score: %d / %d%n%n", score, problems.size()));
        for (int i = 0; i < problems.size(); i++) {
            Problem p = problems.get(i);
            String ans = userInputs.get(i);
            boolean correct = ans.matches("-?\\d+") && Integer.parseInt(ans) == p.getAnswer();
            sb.append(String.format("%d. %s = %d   Your answer: %s [%s]%n",
                    i + 1, p.getQuestionString(), p.getAnswer(),
                    ans.isEmpty() ? "(no answer)" : ans, correct ? "Correct" : "Incorrect"));
        }
        JOptionPane.showMessageDialog(this, sb.toString());
    }

    private int rand(int min, int max, Random r) {
        return r.nextInt(max - min + 1) + min;
    }

    private String getRandomOperation(boolean add, boolean sub, boolean mul, boolean div, Random random) {
        ArrayList<String> ops = new ArrayList<>();
        if (add) ops.add("+");
        if (sub) ops.add("-");
        if (mul) ops.add("×");
        if (div) ops.add("÷");
        if (ops.isEmpty()) {
            return "+";
        }
        return ops.get(random.nextInt(ops.size()));
    }

    private static class Problem {
        private final String questionString;
        private final int answer;

        public Problem(String questionString, int answer) {
            this.questionString = questionString;
            this.answer = answer;
        }

        public String getQuestionString() {
            return questionString;
        }

        public int getAnswer() {
            return answer;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MathProblemGeneratorGUI_arraylist gui = new MathProblemGeneratorGUI_arraylist();
            gui.setVisible(true);
        });
    }
}