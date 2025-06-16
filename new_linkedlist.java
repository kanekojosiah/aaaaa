import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;      // ← missing import
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

class MathProblemGeneratorGUI_linkedlist extends JFrame {
    private final JSpinner operandDigitsSpinner = new JSpinner(new SpinnerNumberModel(2, 1, Integer.MAX_VALUE, 1));
    private final JSpinner questionsSpinner     = new JSpinner(new SpinnerNumberModel(5, 1, Integer.MAX_VALUE, 1));
    private final JSpinner operandCountSpinner  = new JSpinner(new SpinnerNumberModel(2, 2, 10, 1));

    private final JCheckBox additionCheck       = new JCheckBox("Addition (+)");
    private final JCheckBox subtractionCheck    = new JCheckBox("Subtraction (-)");
    private final JCheckBox multiplicationCheck = new JCheckBox("Multiplication (×)");
    private final JCheckBox divisionCheck       = new JCheckBox("Division (÷)");
    private final JCheckBox mixedCheck          = new JCheckBox("Mixed (Random)");

    private final JTextField answerField        = new JTextField(10);
    private final JLabel questionLabel          = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel progressLabel          = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel feedbackLabel          = new JLabel(" ", SwingConstants.CENTER);

    private final JButton nextButton            = new JButton("Next Question");
    private final JButton generateButton        = new JButton("Start New Quiz");

    /* ──────────────── data ──────────────── */
    private final List<Problem> problems  = new LinkedList<>();
    private final List<String>  userInputs = new ArrayList<>();   // indexed same as problems

    private final DefaultListModel<String> questionListModel = new DefaultListModel<>();
    private JList<String> questionList;

    private int currentIndex = 0;
    private int score        = 0;

    /* ──────────────── ctor ──────────────── */
    MathProblemGeneratorGUI_linkedlist() { initializeUI(); }

    /* ─────────────────────────────────────── */
    /*   U I   L A Y O U T                    */
    /* ─────────────────────────────────────── */
    private void initializeUI() {

        setTitle("Math Problem Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        /* left: list of questions */
        JPanel leftPanel = new JPanel(new BorderLayout());
        questionList = new JList<>(questionListModel);
        questionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                currentIndex = questionList.getSelectedIndex();
                if (currentIndex >= 0 && currentIndex < problems.size()) showSelectedQuestion();
            }
        });
        leftPanel.add(new JScrollPane(questionList), BorderLayout.CENTER);

        /* north: spinners */
        JPanel controlPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.add(new JLabel("Number of Digits per operand (min 1):"));
        controlPanel.add(operandDigitsSpinner);
        controlPanel.add(new JLabel("Number of Questions (min 1):"));
        controlPanel.add(questionsSpinner);
        controlPanel.add(new JLabel("Number of Operands (min 2):"));
        controlPanel.add(operandCountSpinner);

        /* operations check‑boxes */
        JPanel operationsPanel = new JPanel();
        operationsPanel.setBorder(BorderFactory.createTitledBorder("Operations"));
        operationsPanel.add(additionCheck);
        operationsPanel.add(subtractionCheck);
        operationsPanel.add(multiplicationCheck);
        operationsPanel.add(divisionCheck);
        operationsPanel.add(mixedCheck);
        mixedCheck.addActionListener(e -> {
            boolean mixed = mixedCheck.isSelected();
            additionCheck.setEnabled(!mixed);
            subtractionCheck.setEnabled(!mixed);
            multiplicationCheck.setEnabled(!mixed);
            divisionCheck.setEnabled(!mixed);
        });

        /* center: current question & feedback */
        JPanel questionPanel = new JPanel(new BorderLayout(10, 10));
        questionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        questionPanel.add(progressLabel, BorderLayout.NORTH);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 24));
        questionPanel.add(questionLabel, BorderLayout.CENTER);
        feedbackLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        questionPanel.add(feedbackLabel, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(operationsPanel, BorderLayout.NORTH);
        centerPanel.add(questionPanel,   BorderLayout.CENTER);

        /* south: answer field */
        JPanel answerPanel = new JPanel();
        answerPanel.add(new JLabel("Your answer: "));
        answerPanel.add(answerField);
        nextButton.addActionListener(this::nextQuestion);
        nextButton.setEnabled(false);
        answerPanel.add(nextButton);

        /* east: generate button */
        generateButton.addActionListener(this::generateProblems);

        /* add to frame */
        add(controlPanel,  BorderLayout.NORTH);
        add(leftPanel,     BorderLayout.WEST);
        add(centerPanel,   BorderLayout.CENTER);
        add(generateButton,BorderLayout.EAST);
        add(answerPanel,   BorderLayout.SOUTH);

        setSize(700, 420);
        setLocationRelativeTo(null);
    }

    /* ─────────────────────────────────────── */
    /*   Q U I Z   L O G I C                  */
    /* ─────────────────────────────────────── */

    private void generateProblems(ActionEvent e) {

        /* ensure at least one op chosen */
        if (!(additionCheck.isSelected() || subtractionCheck.isSelected()
                || multiplicationCheck.isSelected() || divisionCheck.isSelected() || mixedCheck.isSelected())) {
            JOptionPane.showMessageDialog(this, "Please select at least one operation!");
            return;
        }

        /* reset state */
        problems.clear();
        userInputs.clear();
        score = 0;

        int totalQuestions = (int) questionsSpinner.getValue();
        int digits         = (int) operandDigitsSpinner.getValue();
        int operandCount   = (int) operandCountSpinner.getValue();

        int min = (digits == 1) ? 0 : (int) Math.pow(10, digits - 1);
        int max = (int) Math.pow(10, digits) - 1;

        Random rnd = new Random();
        questionListModel.clear();

        while (problems.size() < totalQuestions) {

            /* decide operation */
            String op = mixedCheck.isSelected()
                    ? getRandomOperation(true, true, true, true, rnd)
                    : getRandomOperation(
                    additionCheck.isSelected(),
                    subtractionCheck.isSelected(),
                    multiplicationCheck.isSelected(),
                    divisionCheck.isSelected(),
                    rnd);

            /* build operand list */
            List<Integer> operands = new ArrayList<>(operandCount);
            for (int i = 0; i < operandCount; i++) operands.add(rand(min, max, rnd));

            /* compute result & build string */
            int result = operands.get(0);
            StringBuilder sb = new StringBuilder(String.valueOf(operands.get(0)));

            for (int i = 1; i < operandCount; i++) {
                int val = operands.get(i);

                switch (op) {
                    case "+" -> result += val;
                    case "-" -> result -= val;
                    case "×" -> {
                        /* we allow multi‑operand × but avoid overflow by capping operands */
                        result *= val;
                    }
                    case "÷" -> {
                        if (val == 0) { i--; continue; }   // regenerate operand if zero
                        /* make dividend exactly divisible by val for integer result */
                        int dividend = result * val;
                        result = dividend / val;           // stays as 'result'
                        operands.set(0, dividend);        // update first operand
                    }
                }
                sb.append(" ").append(op).append(" ").append(val);
            }

            String qStr = sb.toString();
            problems.add(new Problem(qStr, result));
            questionListModel.addElement(qStr + " =");
            userInputs.add("");                     // keep lists in‑sync
        }

        currentIndex = 0;
        if (!problems.isEmpty()) {
            questionList.setSelectedIndex(0);
            nextButton.setEnabled(true);
            showSelectedQuestion();
        }
    }

    private void showSelectedQuestion() {
        if (currentIndex < 0 || currentIndex >= problems.size()) return;

        Problem p = problems.get(currentIndex);
        progressLabel.setText("Question " + (currentIndex + 1) + " / " + problems.size());
        questionLabel.setText(p.q() + " =");
        feedbackLabel.setText(" ");
        answerField.setText(userInputs.get(currentIndex));
        answerField.requestFocus();
    }

    private void nextQuestion(ActionEvent e) {

        if (currentIndex < 0 || currentIndex >= problems.size()) return;

        Problem current = problems.get(currentIndex);
        String input = answerField.getText().trim();
        userInputs.set(currentIndex, input.isEmpty() ? "(no answer)" : input);

        String fb;
        try {
            int userAns = Integer.parseInt(input);
            if (userAns == current.a()) {
                score++;
                fb = "Correct! Ans: " + current.a();
            } else {
                fb = "Incorrect. Ans: " + current.a();
            }
        } catch (NumberFormatException ex) {
            fb = "Invalid input. Ans: " + current.a();
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

    private void showResults() {
        StringBuilder sb = new StringBuilder("Final Score: " + score + "/" + problems.size() + "\n\n");
        for (int i = 0; i < problems.size(); i++) {
            Problem p = problems.get(i);
            String ans = userInputs.get(i);
            boolean correct = ans.matches("-?\\d+") && Integer.parseInt(ans) == p.a();
            sb.append(String.format(
                    "%d. %s = %d   Your answer: %s [%s]%n",
                    i + 1, p.q(), p.a(), ans, correct ? "Correct" : "Incorrect"));
        }
        JOptionPane.showMessageDialog(this, sb.toString());
    }

    /* ─────────────────────────────────────── */
    /*   U T I L                              */
    /* ─────────────────────────────────────── */
    private int rand(int min, int max, Random r) { return r.nextInt(max - min + 1) + min; }

    private String getRandomOperation(boolean add, boolean sub, boolean mul, boolean div, Random r) {
        List<String> ops = new ArrayList<>();
        if (add) ops.add("+");
        if (sub) ops.add("-");
        if (mul) ops.add("×");
        if (div) ops.add("÷");
        return ops.get(r.nextInt(ops.size()));
    }

    /* ─────────────────────────────────────── */
    /*   R E C O R D                          */
    /* ─────────────────────────────────────── */
    private record Problem(String q, int a) {}

    /* ─────────────────────────────────────── */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MathProblemGeneratorGUI_linkedlist().setVisible(true));
    }
}
