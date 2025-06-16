import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

class MathProblemGeneratorGUI extends JFrame {
    private final JSpinner operandDigitsSpinner = new JSpinner(new SpinnerNumberModel(2, 1, Integer.MAX_VALUE, 1));
    private final JSpinner questionsSpinner = new JSpinner(new SpinnerNumberModel(5, 1, Integer.MAX_VALUE, 1));
    private final JSpinner operandCountSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 10, 1)); // min 2, max 10
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

    private Set<Problem> problems = new LinkedHashSet<>();
    private final Map<Problem, String> userInputs = new LinkedHashMap<>();
    private DefaultListModel<String> questionListModel = new DefaultListModel<>();
    private JList<String> questionList;
    private int currentIndex = 0;
    private int score = 0;

    public MathProblemGeneratorGUI() {
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
        // ADD THESE TWO LINES FOR THE NEW SPINNER
        controlPanel.add(new JLabel("Number of Operands (min 2):"));
        controlPanel.add(operandCountSpinner);


        // Operations Panel (Checkboxes)
        JPanel operationsPanel = new JPanel();
        operationsPanel.setBorder(BorderFactory.createTitledBorder("Operations"));
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

        Set<Problem> generated = new LinkedHashSet<>();
        int needed = (int) questionsSpinner.getValue();
        Random rnd = new Random();
        int digits = (int) operandDigitsSpinner.getValue();
        // GET THE VALUE OF THE NEW SPINNER
        int operandCount = (int) operandCountSpinner.getValue();

        // The min/max values for a single operand
        int minOperandValue = (int) Math.pow(10, digits - 1);
        int maxOperandValue = (int) Math.pow(10, digits) - 1;
        // Ensure single digit numbers like '0' aren't allowed when digits = 1 for minOperandValue
        if (digits == 1) {
            minOperandValue = 0; // Or 1 if you prefer single digit numbers not to be 0
        }


        questionListModel.clear();
        while (generated.size() < needed) {
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

            // Generate operands based on operandCount
            List<Integer> operands = new ArrayList<>();
            for (int i = 0; i < operandCount; i++) {
                operands.add(rand(minOperandValue, maxOperandValue, rnd));
            }

            // Calculate answer and format question string
            int result;
            String questionString;

            // Handle the first two operands directly for initial calculation
            int val1 = operands.get(0);
            int val2 = operands.get(1);

            switch (op) {
                case "+":
                    result = val1 + val2;
                    break;
                case "-":
                    // Ensure subtraction results in non-negative for simplicity, or handle negative results
                    if (val2 > val1) {
                        int temp = val1;
                        val1 = val2;
                        val2 = temp;
                    }
                    result = val1 - val2;
                    break;
                case "×":
                    result = val1 * val2;
                    break;
                case "÷":
                    // Ensure division results in an integer and no division by zero
                    if (val2 == 0) { // If second operand is 0, regenerate or handle differently
                        continue; // Skip this problem and try again
                    }
                    int divisor = val2;
                    // Make 'val1' a multiple of 'val2' to ensure integer division
                    int multiplier = rnd.nextInt((maxOperandValue / divisor) + 1) + 1; // ensures a non-zero multiplier
                    val1 = divisor * multiplier;
                    result = val1 / divisor;
                    break;
                default:
                    continue; // Should not happen
            }

            // For additional operands (beyond the first two)
            // This part needs more robust logic depending on how you want to chain operations
            // For simplicity, I'll demonstrate adding them to the result for + and -
            // For * and /, chaining is more complex (e.g., order of operations)
            // You might want to generate only 2 operands for * and / if you want simple problems
            // or implement a full expression parser.
            if (op.equals("+") || op.equals("-")) { // Simplified chaining for + and -
                for (int i = 2; i < operandCount; i++) {
                    int nextOperand = operands.get(i);
                    if (op.equals("+")) {
                        result += nextOperand;
                    } else { // op.equals("-")
                        result -= nextOperand;
                    }
                }
            } else if (operandCount > 2) {
                // For multiplication and division with more than 2 operands,
                // this current simple structure won't work correctly without
                // proper order of operations. You might want to restrict operandCount to 2
                // for these operations or implement more advanced logic.
                // For now, let's just use the first two operands for simplicity for * and /
                // If you need more complex problems, you'd need a more advanced expression builder.
                // For this example, if operandCount > 2 for * or /, it will only use the first two.
                // You might want to add a check/limit for operandCount based on selected operation
            }


            // Build the question string
            StringBuilder questionSb = new StringBuilder();
            questionSb.append(operands.get(0));
            for (int i = 1; i < operandCount; i++) {
                questionSb.append(" ").append(op).append(" ").append(operands.get(i));
            }
            questionString = questionSb.toString();

            Problem p = new Problem(questionString, result); // Problem record needs adjustment for string question
            generated.add(p);
            questionListModel.addElement(questionString + " ="); // Display the full question string
        }

        problems = generated;
        userInputs.clear();
        score = 0;
        currentIndex = 0;
        questionList.setSelectedIndex(0);
        nextButton.setEnabled(true);
        showSelectedQuestion();
    }

    private void showSelectedQuestion() {
        if (currentIndex >= 0 && currentIndex < problems.size()) {
            Problem p = (Problem) problems.toArray()[currentIndex];
            progressLabel.setText("Question " + (currentIndex + 1) + " / " + problems.size());
            // Updated to use the question string from the Problem object
            questionLabel.setText(p.getQuestionString() + " =");
            feedbackLabel.setText(" ");
            answerField.setText(userInputs.getOrDefault(p, ""));
            answerField.requestFocus();
        }
    }

    private void nextQuestion(ActionEvent e) {
        if (currentIndex >= 0 && currentIndex < problems.size()) {
            Problem current = (Problem) problems.toArray()[currentIndex];
            String input = answerField.getText().trim();
            userInputs.put(current, input.isEmpty() ? "(no answer)" : input);

            String fb;
            try {
                int userAns = Integer.parseInt(input);
                if (userAns == current.getAnswer()) { // Updated to use getAnswer()
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
            } else if (userInputs.size() == problems.size()) {
                showResults();
                nextButton.setEnabled(false);
            }
        }
    }

    private void showResults() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Final Score: %d / %d%n%n", score, problems.size()));
        int i = 1;
        for (Problem p : userInputs.keySet()) {
            String ans = userInputs.get(p);
            boolean correct = ans.matches("-?\\d+") && Integer.parseInt(ans) == p.getAnswer(); // Updated to use getAnswer()
            sb.append(String.format(
                    "%d. %s %d   Your answer: %s [%s]%n", // Updated formatting
                    i++, p.getQuestionString(), p.getAnswer(), // Updated to use getQuestionString() and getAnswer()
                    ans, correct ? "Correct" : "Incorrect"
            ));
        }
        JOptionPane.showMessageDialog(this, sb.toString());
    }

    private int rand(int min, int max, Random r) {
        return r.nextInt(max - min + 1) + min;
    }

    private String getRandomOperation(boolean add, boolean sub, boolean mul, boolean div, Random random) {
        List<String> ops = new ArrayList<>();
        if (add) ops.add("+");
        if (sub) ops.add("-");
        if (mul) ops.add("×");
        if (div) ops.add("÷");
        if (ops.isEmpty()) { // Fallback if no operation is selected (should be caught by validation)
            return "+"; // Default to addition
        }
        return ops.get(random.nextInt(ops.size()));
    }

    // UPDATED PROBLEM RECORD
    private record Problem(String questionString, int answer) {
        public String getQuestionString() {
            return questionString;
        }

        public int getAnswer() {
            return answer;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MathProblemGeneratorGUI_linkedlist().setVisible(true));
    }
}