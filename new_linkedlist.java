import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.Random;

class MathProblemGeneratorGUI_linkedlist extends JFrame {
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

    private final LinkedList<Problem> problems = new LinkedList<>();
    private final LinkedList<String> userInputs = new LinkedList<>();
    private DefaultListModel<String> questionListModel = new DefaultListModel<>();
    private JList<String> questionList;
    private int currentIndex = 0;
    private int score = 0;

    public MathProblemGeneratorGUI_linkedlist() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Math Problem Generator (LinkedList)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

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

        JPanel controlPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.add(new JLabel("Number of Digits per operand (min 1):"));
        controlPanel.add(operandDigitsSpinner);
        controlPanel.add(new JLabel("Number of Questions (min 1):"));
        controlPanel.add(questionsSpinner);
        controlPanel.add(new JLabel("Number of Operands (min 2):"));
        controlPanel.add(operandCountSpinner);

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

        JPanel questionPanel = new JPanel(new BorderLayout(10, 10));
        questionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        questionPanel.add(progressLabel, BorderLayout.NORTH);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 24));
        questionPanel.add(questionLabel, BorderLayout.CENTER);
        feedbackLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        questionPanel.add(feedbackLabel, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(operationsPanel, BorderLayout.NORTH);
        centerPanel.add(questionPanel, BorderLayout.CENTER);

        JPanel answerPanel = new JPanel();
        answerPanel.add(new JLabel("Your answer: "));
        answerPanel.add(answerField);
        nextButton.addActionListener(this::nextQuestion);
        nextButton.setEnabled(false);
        answerPanel.add(nextButton);

        generateButton.addActionListener(this::generateProblems);

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

        int digits = (int) operandDigitsSpinner.getValue();
        int operandCount = (int) operandCountSpinner.getValue();

        // Prevent impossible multiplication settings
        if ((multiplicationCheck.isSelected() || mixedCheck.isSelected()) && digits > 5) {
            JOptionPane.showMessageDialog(this,
                    "Multiplication with more than 5 digits per operand will always overflow.\n" +
                            "Please select 5 or fewer digits for multiplication problems.",
                    "Invalid Settings", JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        problems.clear();
        userInputs.clear();
        int needed = (int) questionsSpinner.getValue();
        Random rnd = new Random();

        int min = (int) Math.pow(10, digits - 1);
        int max = (int) Math.pow(10, digits) - 1;
        if (digits == 1) min = 0;

        questionListModel.clear();
        int maxAttempts = needed * 20;
        int attempts = 0;
        while (problems.size() < needed && attempts < maxAttempts) {
            attempts++;
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

            LinkedList<Integer> operands = new LinkedList<>();
            for (int i = 0; i < operandCount; i++) operands.add(rand(min, max, rnd));

            int result = operands.get(0);
            boolean failed = false;
            StringBuilder sb = new StringBuilder(String.valueOf(operands.get(0)));
            int opsDone = 0;

            for (int i = 1; i < operandCount; i++) {
                int val = operands.get(i);
                try {
                    switch (op) {
                        case "+":
                            result = Math.addExact(result, val);
                            sb.append(" + ").append(val);
                            opsDone++;
                            break;
                        case "-":
                            result = Math.subtractExact(result, val);
                            sb.append(" - ").append(val);
                            opsDone++;
                            break;
                        case "×":
                            result = Math.multiplyExact(result, val);
                            sb.append(" × ").append(val);
                            opsDone++;
                            break;
                        case "÷":
                            if (val == 0) { failed = true; break; }
                            if (result % val != 0) { failed = true; break; }
                            result /= val;
                            sb.append(" ÷ ").append(val);
                            opsDone++;
                            break;
                    }
                } catch (ArithmeticException ex) {
                    failed = true;
                    break;
                }
                if (failed) break;
            }

            if (!failed && opsDone > 0) {
                String questionString = sb.toString();
                problems.add(new Problem(questionString, result));
                questionListModel.addElement(questionString + " =");
                userInputs.add("");
            }
        }

        if (problems.size() < needed) {
            JOptionPane.showMessageDialog(this,
                    "Unable to generate quiz with these settings (too many overflows or impossible operations). " +
                            "Try reducing the number of digits or operands, or avoid multiplication.",
                    "Generation Failed", JOptionPane.ERROR_MESSAGE
            );
            nextButton.setEnabled(false);
            return;
        }

        currentIndex = 0;
        questionList.setSelectedIndex(0);
        nextButton.setEnabled(true);
        score = 0;
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
            int correctAns = current.getAnswer();
            String correctAnsDisplay;
            if (correctAns == Integer.MAX_VALUE) {
                correctAnsDisplay = "Overflow (too big)";
            } else if (correctAns == Integer.MIN_VALUE) {
                correctAnsDisplay = "Underflow (too small/negative)";
            } else {
                correctAnsDisplay = String.valueOf(correctAns);
            }

            try {
                int userAns = Integer.parseInt(input);
                if (userAns == correctAns) {
                    score++;
                    fb = "Correct! Ans: " + correctAnsDisplay;
                } else {
                    fb = "Incorrect. Ans: " + correctAnsDisplay;
                }
            } catch (NumberFormatException ex) {
                fb = "Invalid input. Ans: " + correctAnsDisplay;
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
            int correctAns = p.getAnswer();
            String correctAnsDisplay;
            if (correctAns == Integer.MAX_VALUE) {
                correctAnsDisplay = "Overflow (too big)";
            } else if (correctAns == Integer.MIN_VALUE) {
                correctAnsDisplay = "Underflow (too small/negative)";
            } else {
                correctAnsDisplay = String.valueOf(correctAns);
            }
            boolean correct = ans.matches("-?\\d+") && Integer.parseInt(ans) == correctAns;
            sb.append(String.format("%d. %s = %s   Your answer: %s [%s]%n",
                    i + 1, p.getQuestionString(), correctAnsDisplay,
                    ans.isEmpty() ? "(no answer)" : ans, correct ? "Correct" : "Incorrect"));
        }
        JOptionPane.showMessageDialog(this, sb.toString());
    }

    private int rand(int min, int max, Random r) {
        return r.nextInt(max - min + 1) + min;
    }

    private String getRandomOperation(boolean add, boolean sub, boolean mul, boolean div, Random random) {
        LinkedList<String> ops = new LinkedList<>();
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
            MathProblemGeneratorGUI_linkedlist gui = new MathProblemGeneratorGUI_linkedlist();
            gui.setVisible(true);
        });
    }
}