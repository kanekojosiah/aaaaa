//Start of imports
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Random;
//End of imports

//class
class MathProblemGeneratorGUI_arraylist extends JFrame {
//    Spinner select number of digits
    private final JSpinner operandDigitsSpinner = new JSpinner(new SpinnerNumberModel(2, 1, Integer.MAX_VALUE, 1));
//    Number of questions
    private final JSpinner questionsSpinner = new JSpinner(new SpinnerNumberModel(5, 1, Integer.MAX_VALUE, 1));
//    Number of operands per question
    private final JSpinner operandCountSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 10, 1));
//    Start of checklist and option for which operation
    private final JCheckBox additionCheck = new JCheckBox("Addition (+)");
    private final JCheckBox subtractionCheck = new JCheckBox("Subtraction (-)");
    private final JCheckBox multiplicationCheck = new JCheckBox("Multiplication (×)");
    private final JCheckBox divisionCheck = new JCheckBox("Division (÷)");
    private final JCheckBox mixedCheck = new JCheckBox("Mixed (Random)");
//    End of checklist and option

//    Text field for user input
    private final JTextField answerField = new JTextField(10);
//    Label for displaying current question
    private final JLabel questionLabel = new JLabel(" ", SwingConstants.CENTER);
//    Display progress
    private final JLabel progressLabel = new JLabel(" ", SwingConstants.CENTER);
//    Feedback
    private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
//    Button to go next or submit
    private final JButton nextButton = new JButton("Next Question");
//    Generate quiz
    private final JButton generateButton = new JButton("Start New Quiz");

//    Storing of question
    private final ArrayList<Problem> problems = new ArrayList<>();
//    Store user answer
    private final ArrayList<String> userInputs = new ArrayList<>();
//    Sidebar to view question functionality
    private DefaultListModel<String> questionListModel = new DefaultListModel<>();
//    Ui display list of question
    private JList<String> questionList;
//   Index of current question
    private int currentIndex = 0;
//    Number of correct answer
    private int score = 0;

//    Constructor initialize
    public MathProblemGeneratorGUI_arraylist() {
//        Call method setup user interface
        initializeUI();
    }


    private void initializeUI() {
//        Ui
        setTitle("Math Problem Generator");
//        Exit on close
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

//        List questions
        JPanel leftPanel = new JPanel(new BorderLayout());
//        Jlist to display question list
        questionList = new JList<>(questionListModel);
//        Single selection
        questionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        When user click question, update currentIndex and display
        questionList.addListSelectionListener(e -> {
//            Check if final
            if (!e.getValueIsAdjusting()) {
//                Get selected index
                currentIndex = questionList.getSelectedIndex();
//                Validate index
                if (currentIndex >= 0 && currentIndex < problems.size()) {
//                    Display selected question
                    showSelectedQuestion();
                }
            }
        });
//        Scroll functionality
        JScrollPane scrollPane = new JScrollPane(questionList);
//        Scroll pane to left panel
        leftPanel.add(scrollPane, BorderLayout.CENTER);

//        Layout control
//        display the ui
        JPanel controlPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.add(new JLabel("Number of Digits per operand (min 1):"));
//        Spinner for digits
        controlPanel.add(operandDigitsSpinner);
        controlPanel.add(new JLabel("Number of Questions (min 1):"));
//        Spinner for question
        controlPanel.add(questionsSpinner);
        controlPanel.add(new JLabel("Number of Operands (min 2):"));
//        Spinner for operand
        controlPanel.add(operandCountSpinner);

//        Panel opration checkboxes
        JPanel operationsPanel = new JPanel();
        operationsPanel.setBorder(BorderFactory.createTitledBorder("Operations"));
        operationsPanel.setLayout(new BoxLayout(operationsPanel, BoxLayout.Y_AXIS));
//        Start of add check boxes
        operationsPanel.add(additionCheck);
        operationsPanel.add(subtractionCheck);
        operationsPanel.add(multiplicationCheck);
        operationsPanel.add(divisionCheck);
        operationsPanel.add(mixedCheck);
//        End

//        If mixed selected, disable everything else
        mixedCheck.addActionListener(e -> {
            boolean isMixed = mixedCheck.isSelected();
            additionCheck.setEnabled(!isMixed);
            subtractionCheck.setEnabled(!isMixed);
            multiplicationCheck.setEnabled(!isMixed);
            divisionCheck.setEnabled(!isMixed);
        });

//        Center panel, display current question, progress, and feedback
        JPanel questionPanel = new JPanel(new BorderLayout(10, 10));
        questionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        questionPanel.add(progressLabel, BorderLayout.NORTH);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 24));
        questionPanel.add(questionLabel, BorderLayout.CENTER);
        feedbackLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        questionPanel.add(feedbackLabel, BorderLayout.SOUTH);

//        Panel display
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(operationsPanel, BorderLayout.NORTH);
        centerPanel.add(questionPanel, BorderLayout.CENTER);

//        Answer input and navigation
//        Panel for answer input
        JPanel answerPanel = new JPanel();
//        Label answer input for user to see
        answerPanel.add(new JLabel("Your answer: "));
//        Answer text field
        answerPanel.add(answerField);
//        Next button
        nextButton.addActionListener(this::nextQuestion);
//        DISABLED until when uhh quiz is generated
        nextButton.setEnabled(false);
//        Next button
        answerPanel.add(nextButton);

//        Panels
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
//        Select one operation requirement
        if (!(additionCheck.isSelected() || subtractionCheck.isSelected()
                || multiplicationCheck.isSelected() || divisionCheck.isSelected() || mixedCheck.isSelected())) {
            JOptionPane.showMessageDialog(this, "Please select at least one operation!");
            return;
        }

        int digits = (int) operandDigitsSpinner.getValue();
        int operandCount = (int) operandCountSpinner.getValue();

//        Overflow multiplication error error handling
        if ((multiplicationCheck.isSelected() || mixedCheck.isSelected()) && digits > 5) {
            JOptionPane.showMessageDialog(this,
                    "Multiplication with more than 5 digits per operand will always overflow.\n" +
                            "Please select 5 or fewer digits for multiplication problems.",
                    "Invalid Settings", JOptionPane.WARNING_MESSAGE
            );
            return;
        }
//Clear previous problem
        problems.clear();
//        Clear previous input
        userInputs.clear();
//        Get number of questions to generate
        int needed = (int) questionsSpinner.getValue();
//        Create random number generator
        Random rnd = new Random();

//        Min/max number in digit range
        int minOperandValue = (int) Math.pow(10, digits - 1);
        int maxOperandValue = (int) Math.pow(10, digits) - 1;
        if (digits == 1) {
            minOperandValue = 0;
        }
//Clear question lisst model
        questionListModel.clear();
//        Cap attempt to avoid infinite loops
        int maxAttempts = needed * 20;
//        Initialize counter
        int attempts = 0;
//        Loop until enough problems are generated
        while (problems.size() < needed && attempts < maxAttempts) {
//            Increment attempt
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
// Generate random operand
//            List to hold
            ArrayList<Integer> operands = new ArrayList<>();
// Add random operand
            for (int i = 0; i < operandCount; i++) {
                operands.add(rand(minOperandValue, maxOperandValue, rnd));
            }
// Start with first operand
            int result = operands.get(0);
//            If generation fails, overflow
            boolean failed = false;
            StringBuilder questionSb = new StringBuilder();
            questionSb.append(operands.get(0));
//            Count of operations performed
            int opsDone = 0;

//            Apply the chosen operation for each additional operand
            for (int i = 1; i < operands.size(); i++) {
//                Get next operand
                int nextVal = operands.get(i);
                try {
                    switch (op) {
//                        Perform operation based on selected operator
                        case "+":
//                            Check overflow
                            result = Math.addExact(result, nextVal);
//                            Append
                            questionSb.append(" + ").append(nextVal);
//                            Increment
                            opsDone++;
                            break;
                        case "-":
//                            Check for overflow
                            result = Math.subtractExact(result, nextVal);
                            questionSb.append(" - ").append(nextVal);
                            opsDone++;
                            break;
                        case "×":
//                            Check for overflow
                            result = Math.multiplyExact(result, nextVal);
                            questionSb.append(" × ").append(nextVal);
                            opsDone++;
                            break;
                        case "÷":
//                            Division by zero error handling
                            if (nextVal == 0) { failed = true; break; }
                            if (result % nextVal != 0) { failed = true; break; }
                            result /= nextVal;
                            questionSb.append(" ÷ ").append(nextVal);
                            opsDone++;
                            break;
                    }
                }
//                Overflow exception
                catch (ArithmeticException ex) {
                    failed = true;
                    break;
                }
                if (failed) break;
            }
//            Addproblem if no error and at least one opration done
            if (!failed && opsDone > 0) {
                String questionString = questionSb.toString();
                problems.add(new Problem(questionString, result));
                questionListModel.addElement(questionString + " =");
                userInputs.add("");
            }
//            Otherwise skip
        }

//        If not enough problem generated, display error and stop execution
        if (problems.size() < needed) {
            JOptionPane.showMessageDialog(this,
                    "Unable to generate quiz with these settings (too many overflows or impossible operations). " +
                            "Try reducing the number of digits or operands, or avoid multiplication.",
                    "Generation Failed", JOptionPane.ERROR_MESSAGE
            );
            nextButton.setEnabled(false);
            return;
        }

//        Initialize
        currentIndex = 0;
        questionList.setSelectedIndex(0);
        nextButton.setEnabled(true);
        score = 0;
        showSelectedQuestion();
    }

    private void showSelectedQuestion() {
//        Validation
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
//        Validate
        if (currentIndex >= 0 && currentIndex < problems.size()) {
            Problem current = problems.get(currentIndex);
            String input = answerField.getText().trim();
            userInputs.set(currentIndex, input);

//            Feedback
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
//Check user's answer
            try {
                int userAns = Integer.parseInt(input);
//                If answer is correct
                if (userAns == correctAns) {
//                    Increment
                    score++;
                    fb = "Correct! Ans: " + correctAnsDisplay;
                } else {
                    fb = "Incorrect. Ans: " + correctAnsDisplay;
                }
            } catch (NumberFormatException ex) {
//                Set feedback for invalid input
                fb = "Invalid input. Ans: " + correctAnsDisplay;
            }
            feedbackLabel.setText(fb);
//Moven ext question or show result
            if (currentIndex < problems.size() - 1) {
                currentIndex++;
                questionList.setSelectedIndex(currentIndex);
                showSelectedQuestion();
            }
            else {
//                Show when no more questions
                showResults();
                nextButton.setEnabled(false);
            }
        }
    }
//At end of quiz or questions, display all result and feedback
    private void showResults() {
        StringBuilder sb = new StringBuilder();
//        Append
        sb.append(String.format("Final Score: %d / %d%n%n", score, problems.size()));
        for (int i = 0; i < problems.size(); i++) {
//            Get problem, input, answer, correct answer, etc.
            Problem p = problems.get(i);
            String ans = userInputs.get(i);
            int correctAns = p.getAnswer();
            String correctAnsDisplay;
            if (correctAns == Integer.MAX_VALUE) {
//                Overflow
                correctAnsDisplay = "Overflow (too big)";
            } else if (correctAns == Integer.MIN_VALUE) {
//                Underflow feedback
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

//    Generate random integer min max (inclusive)
    private int rand(int min, int max, Random r) {
        return r.nextInt(max - min + 1) + min;
    }

    private String getRandomOperation(boolean add, boolean sub, boolean mul, boolean div, Random random) {
//        lIst to hold operation
        ArrayList<String> ops = new ArrayList<>();
//Adding the operations if allowed
        if (add) ops.add("+");
        if (sub) ops.add("-");
        if (mul) ops.add("×");
        if (div) ops.add("÷");
//        If no operation allowed
        if (ops.isEmpty()) {
//            Default
            return "+";
        }
//        Return random operation from list
        return ops.get(random.nextInt(ops.size()));
    }

//    Store question string and correct answer
    private static class Problem {
//        String of the question
        private final String questionString;
//        Correct answer to question
        private final int answer;

//        Constructor initialize
        public Problem(String questionString, int answer) {
//            Set string and answer
            this.questionString = questionString;
            this.answer = answer;
        }
//Getter method
        public String getQuestionString() {
//            Return question string
            return questionString;
        }
//Return answer
        public int getAnswer() {
            return answer;
        }
    }
//Start gui
    public static void main(String[] args) {
//        Ensure GUI creation is done on execution
        SwingUtilities.invokeLater(() -> {
            MathProblemGeneratorGUI_arraylist gui = new MathProblemGeneratorGUI_arraylist();
//           Make gui visible
            gui.setVisible(true);
        });
    }
}