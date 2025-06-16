// java imports
import java.util.*;
// java import for handling large integers
import java.math.BigInteger;
// There is a limit on the generation on the questions
// As the program will run out of memory allocation space, can be increased by "-Xmx{number}g"

// Starting codes of main program functionality
public class MathGen_Performance_Time_Tester {

//    This code displays how many questions wished to see as a preview
    private static final int question_preview_generated = 5;
//    Max digits for longvalue types
    private static final int digit_limit_longvalue = 18;
//    "Random" object for the generation of random numbers
    private static final Random generation = new Random();

    public static void main(String[] args) {
//        Scanner to read input
        Scanner sc = new Scanner(System.in);
//        Start of UI
        System.out.println("Math Problem Generator Performance Tester");
        System.out.println("----------------------------------------");

        System.out.print("Questions to generate (e.g. 100000): ");
//        qCount stands for no of questions from user input
        int qCount = sc.nextInt();

        System.out.print("Digits per operand (min 1): ");
//        obtain user input for digits with at least of digit 1
        int digits = Math.max(1, sc.nextInt());

        System.out.print("Operands per question (min 2): ");
//        No of operands per question with limit of 2 operands
        int operation = Math.max(2, sc.nextInt());

        System.out.print("Operation (+, -, *, /, mix): ");
//        Reads what operation type used and will lowercase input automatically
        String getOperator = sc.next().trim().toLowerCase();
// Calls testing method for the different data structures
        testing("ArrayList", new ArrayList<>(), qCount, digits, operation, getOperator);
        testing("LinkedList", new LinkedList<>(), qCount, digits, operation, getOperator);
        testing("HashSet", new HashSet<>(), qCount, digits, operation, getOperator);
    }

    private static void testing(String name, Collection<Problem> container, int count, int digits, int operands, String operation_kind) {
//     Feedback on which data structure will be tested
        System.out.printf("%n=== %s ===%n", name);

//        Due to a lot of errors in terms of space, a sort of "garbage" bin is used to better optimize performance
//        Will remove objects that are no longer used by program
        System.gc();
//        Initial memory usage
        long memory_initial = memory_usage();
//        Start time for performance measurement
        long start_time = System.nanoTime();

//        Generate problem and add to container
        for (int i = 0; i < count; i++) container.add(generate_problems(digits, operands, operation_kind));

//        Initiate "garbage" again
        System.gc();
//        Calculate time adding problems
        long time_res = System.nanoTime() - start_time;
//        Memory usage after adding problem
        long mem1 = memory_usage();
//        Memory increase calculation
        long mem_inc = Math.max(0, mem1 - memory_initial);

//        Average memory per item, but I do not think will be shown for report not sure

        double perItem = container.isEmpty() ? 0 : (double) mem_inc / container.size();

//        feedback UI of result
        System.out.printf("Time    : %.2f ms  (%.2f µs/obj)%n", time_res / 1_000_000.0, (time_res / 1000.0) / count);
        System.out.printf("Memory  : %,d bytes (%.1f bytes/obj)%n", mem_inc, perItem);

//        Shows the time and space complexity
        System.out.println("Complexity: O(n) time, O(n) space\n");

        System.out.println("Preview problems:");
        container.stream().limit(question_preview_generated).forEach(System.out::println);
    }

    private static Problem generate_problems(int d, int operation, String kind) {
//        Will generate the problems based on given user inputs on how the questions should be generated
//        If no of digits exceed limit, initiate generation of biginteger problem
        if (d > digit_limit_longvalue) return generate_biginteger_problems(d, operation, kind);
//    If not, generate longvalue problems
        return generate_longvalue_problems(d, operation, kind);
    }

    private static Problem generate_longvalue_problems(int d, int operation, String kind) {
//       Initialize
        StringBuilder string_build = new StringBuilder();
//        Longvalue and append
        long res = random_longvalue(d); string_build.append(res);
//        Loop
        for (int i = 1; i < operation; i++) {
//            Get the operator symbol and geenrate longvalue
            char operation1 = operation(kind); long v = random_longvalue(d);
//            Append operator
            string_build.append(' ').append(operation1).append(' ').append(v);
//            Perform and update
            res = perform_program(res, v, operation1);
        }
        return new Problem(string_build.toString(), String.valueOf(res));
    }
//Basically the same that happened to longvalue but now with biginteger
//    I wonder if I still should comment
    private static Problem generate_biginteger_problems(int d, int operation, String kind) {
//      Initialize
        StringBuilder string_build = new StringBuilder();
        BigInteger res = random_biginteger(d); string_build.append(res);
        for (int i = 1; i < operation; i++) {
            char operation1 = operation(kind); BigInteger v = random_biginteger(d);
            string_build.append(' ').append(operation1).append(' ').append(v);
            res = perform_program(res, v, operation1);
        }
        return new Problem(string_build.toString(), res.toString());
    }


    private static char operation(String op) {
//        Operator input based on user input
        return "mix".equals(op) ? "+-*/".charAt(generation.nextInt(4)) : op.charAt(0);
    }

    private static long random_longvalue(int d) {
//        Generate longvalue with no of digits specified
//        One digit, return rand digit
        if (d == 1) return generation.nextInt(10);
//        Calculate minimum value
        long min = (long) Math.pow(10, d - 1);
//        Calculate max value
        long max = (long) Math.pow(10, d) - 1;
//        Return rand longterm with calculated range
        return min + (long) (generation.nextDouble() * (max - min + 1));
    }

//    BigInteger.Ten.pow is a costant
//    Represents value 10 as BigInteger object
//    .pow is integer exponent, so like raising to a number
//    an example of pow would be "BigInteger.TEN.pow(2) means 10 to the power of 2"
    private static BigInteger random_biginteger(int d) {
//
        if (d == 1) return BigInteger.valueOf(generation.nextInt(10));
        BigInteger min = BigInteger.TEN.pow(d - 1);
        BigInteger range = BigInteger.TEN.pow(d).subtract(min);
//        what's different from the longvalue starts here
//        this code line below hold variable  "r"
        BigInteger r;
        do {
//            Generate random BigInteger from specified range
            r = new BigInteger(range.bitLength(), generation);
//            Ensuring generated value is within the range
        } while (r.compareTo(range) > 0);
//        Return generated BigInteger by min value
        return r.add(min);
    }


    private static long memory_usage() {
//        Calculate current memory usage
//        Get runtime object
        Runtime r = Runtime.getRuntime();
//        Return as result different between total and free memory
        return r.totalMemory() - r.freeMemory();
    }

    private static long perform_program(long a, long b, char o) {
//        Switch used to perform which operation
        return switch (o) {
//            Addition
            case '+' -> a + b;
//            Subtraction
            case '-' -> a - b;
//            Multiplication
            case '*' -> a * b;
//            Division, handles division by zero
            default -> (b == 0) ? a : a / b;
        };
    }

    private static BigInteger perform_program(BigInteger a, BigInteger b, char o) {
//        Switch used to perform which operation
        return switch (o) {
//            Addition
            case '+' -> a.add(b);
//            Subtraction
            case '-' -> a.subtract(b);
//            Multiplication
            case '*' -> a.multiply(b);
//            Division, handles division by zero
            default -> b.equals(BigInteger.ZERO) ? a : a.divide(b);
        };
    }

//    a kind of record to hold expression and answer
    private record Problem(String mathExpression, String result) {
//        Override
        public String toString() {
//            Return expression & result
            return mathExpression + " = " + result;
        }
    }
}
