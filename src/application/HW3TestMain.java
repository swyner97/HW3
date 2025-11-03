package application;

import java.sql.SQLException;
import java.util.List;
import databasePart1.*;

/**
 * Automated testing mainline for the Answers class CRUD operations.
 * This class contains five automated tests to verify the functionality
 * of creating, reading, updating, deleting, and searching answers.
 * 
 * <p>This version uses DatabaseHelper for database operations.
 * Tests create necessary question records to satisfy foreign key constraints.
 * 
 * <p>Each test method follows a consistent pattern:
 * <ul>
 *   <li>Setup - Initialize required objects and test data</li>
 *   <li>Execute - Perform the operation being tested</li>
 *   <li>Verify - Check that the operation produced expected results</li>
 *   <li>Return - Return true if test passed, false if failed</li>
 * </ul>
 * 
 * @author Sarah Wyner
 * @version 2.0
 */
public class HW3TestMain {
    
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    /**
     * Main method to run all automated tests.
     * Executes each test sequentially and displays a summary of results.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("    Answers CRUD Operations Test Suite    ");
        System.out.println("===========================================\n");
        
        // Run all five tests
        runTest("Test 1: Create Answer", HW3TestMain::testCreateAnswer);
        runTest("Test 2: Read Answer", HW3TestMain::testReadAnswer);
        runTest("Test 3: Update Answer", HW3TestMain::testUpdateAnswer);
        runTest("Test 4: Delete Answer", HW3TestMain::testDeleteAnswer);
        runTest("Test 5: Search Answers", HW3TestMain::testSearchAnswers);
        
        // Display summary
        System.out.println("\n===========================================");
        System.out.println("              Test Summary                 ");
        System.out.println("===========================================");
        System.out.println("Tests Passed: " + testsPassed);
        System.out.println("Tests Failed: " + testsFailed);
        System.out.println("Total Tests:  " + (testsPassed + testsFailed));
        System.out.println("===========================================\n");
    }
    
    /**
     * Helper method to run a single test and report results.
     * Catches exceptions and marks tests as failed if exceptions occur.
     * 
     * @param testName the name of the test being executed
     * @param test the test function to execute
     */
    private static void runTest(String testName, TestFunction test) {
        System.out.println("Running: " + testName);
        try {
            boolean result = test.execute();
            if (result) {
                System.out.println("✓ PASSED\n");
                testsPassed++;
            } else {
                System.out.println("✗ FAILED\n");
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ FAILED with exception: " + e.getMessage() + "\n");
            e.printStackTrace();
            testsFailed++;
        }
    }
    
    /**
     * Helper method to get an existing question from the database.
     * If no questions exist, creates a test question.
     * 
     * @param db the DatabaseHelper instance
     * @return a valid question ID from the database, or -1 if error
     */
    private static int getValidQuestionId(DatabaseHelper db) {
        List<Question> questions = db.loadAllQs();
        
        if (questions.isEmpty()) {
            System.out.println("  No questions found in database. Please create at least one question first.");
            return -1;
        }
        
        // Use the first available question
        int questionId = questions.get(0).getQuestionId();
        System.out.println("  Using existing question with ID: " + questionId);
        return questionId;
    }
    
    /**
     * Test 1: Create Answer Operation
     * 
     * <p>Verifies that an answer can be successfully created with valid inputs.
     * Tests the create operation by providing a valid userId, questionId, 
     * author name, and content.
     * 
     * <p>Test Steps:
     * <ol>
     *   <li>Initialize DatabaseHelper and connect to database</li>
     *   <li>Ensure test question exists</li>
     *   <li>Record initial number of answers</li>
     *   <li>Create a new answer with valid parameters</li>
     *   <li>Verify the operation indicates success</li>
     *   <li>Verify the answer count increased by one</li>
     *   <li>Verify the created answer has correct attributes</li>
     * </ol>
     * 
     * @return true if the test passes, false otherwise
     */
    public static boolean testCreateAnswer() {
        System.out.println("  Testing answer creation with valid data...");
        
        // Setup - Create DatabaseHelper
        DatabaseHelper db = new DatabaseHelper();
        try {
            db.connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            db.closeConnection();
            return false;
        }
        
        // Get a valid question ID from existing questions
        int questionId = getValidQuestionId(db);
        if (questionId < 0) {
            System.out.println("  Error: Could not find valid question");
            db.closeConnection();
            return false;
        }
        
        // Create Answers instance AFTER question exists
        Answers answers = new Answers(db);
        int initialSize = answers.size();
        
        // Execute - Create a new answer
        Result result = answers.create(1, questionId, "John Doe", "This is a test answer.");
        
        // Verify - Check success
        if (!result.isSuccess()) {
            System.out.println("  Error: Answer creation failed - " + result.getMessage());
            db.closeConnection();
            return false;
        }
        
        if (answers.size() != initialSize + 1) {
            System.out.println("  Error: Answer count did not increase");
            db.closeConnection();
            return false;
        }
        
        Answer createdAnswer = (Answer) result.getData();
        if (createdAnswer == null || !createdAnswer.getAuthor().equals("John Doe")) {
            System.out.println("  Error: Created answer has incorrect data");
            db.closeConnection();
            return false;
        }
        
        System.out.println("  Answer created successfully with ID: " + createdAnswer.getAnswerId());
        db.closeConnection();
        return true;
    }
    
    /**
     * Test 2: Read Answer Operation
     * 
     * <p>Verifies that an existing answer can be retrieved by its ID.
     * Tests the read operation by first creating an answer, then reading it back.
     * 
     * <p>Test Steps:
     * <ol>
     *   <li>Connect to database and ensure test question exists</li>
     *   <li>Create a new answer in database</li>
     *   <li>Retrieve the answer using its ID</li>
     *   <li>Verify the retrieved answer is not null</li>
     *   <li>Verify the retrieved answer matches the created answer</li>
     * </ol>
     * 
     * @return true if the test passes, false otherwise
     */
    public static boolean testReadAnswer() {
        System.out.println("  Testing answer retrieval by ID...");
        
        // Setup - Create DatabaseHelper
        DatabaseHelper db = new DatabaseHelper();
        try {
            db.connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            db.closeConnection();
            return false;
        }
        
        // Get a valid question ID from existing questions
        int questionId = getValidQuestionId(db);
        if (questionId < 0) {
            System.out.println("  Error: Could not find valid question");
            db.closeConnection();
            return false;
        }
        
        // Create Answers instance AFTER question exists
        Answers answers = new Answers(db);
        Result createResult = answers.create(2, questionId, "Jane Smith", "Test answer for reading.");
        
        if (!createResult.isSuccess()) {
            System.out.println("  Error: Failed to create test answer");
            db.closeConnection();
            return false;
        }
        
        Answer createdAnswer = (Answer) createResult.getData();
        int answerId = createdAnswer.getAnswerId();
        
        // Execute - Read the answer
        Answer retrievedAnswer = answers.read(answerId);
        
        // Verify - Check the answer was retrieved correctly
        if (retrievedAnswer == null) {
            System.out.println("  Error: Retrieved answer is null");
            db.closeConnection();
            return false;
        }
        
        if (retrievedAnswer.getAnswerId() != answerId) {
            System.out.println("  Error: Retrieved answer has wrong ID");
            db.closeConnection();
            return false;
        }
        
        if (!retrievedAnswer.getContent().equals("Test answer for reading.")) {
            System.out.println("  Error: Retrieved answer has wrong content");
            db.closeConnection();
            return false;
        }
        
        System.out.println("  Answer retrieved successfully: ID=" + retrievedAnswer.getAnswerId());
        db.closeConnection();
        return true;
    }
    
    /**
     * Test 3: Update Answer Operation
     * 
     * <p>Verifies that an existing answer can be updated with new content.
     * Tests the update operation by creating an answer, then modifying its content
     * and solution status.
     * 
     * <p>Test Steps:
     * <ol>
     *   <li>Connect to database and ensure test question exists</li>
     *   <li>Create a new answer</li>
     *   <li>Create a user with appropriate permissions</li>
     *   <li>Update the answer with new content and solution flag</li>
     *   <li>Verify the update was successful</li>
     *   <li>Verify the answer's content was updated</li>
     *   <li>Verify the answer's solution status was updated</li>
     * </ol>
     * 
     * @return true if the test passes, false otherwise
     */
    public static boolean testUpdateAnswer() {
        System.out.println("  Testing answer update operation...");
        
        // Setup - Create DatabaseHelper
        DatabaseHelper db = new DatabaseHelper();
        try {
            db.connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            db.closeConnection();
            return false;
        }
        
        // Get a valid question ID from existing questions
        int questionId = getValidQuestionId(db);
        if (questionId < 0) {
            System.out.println("  Error: Could not find valid question");
            db.closeConnection();
            return false;
        }
        
        // Create Answers instance AFTER question exists
        Answers answers = new Answers(db);
        Result createResult = answers.create(3, questionId, "Bob Johnson", "Original content");
        
        if (!createResult.isSuccess()) {
            System.out.println("  Error: Failed to create test answer");
            db.closeConnection();
            return false;
        }
        
        Answer createdAnswer = (Answer) createResult.getData();
        int answerId = createdAnswer.getAnswerId();
        
        // Create a user who is the author
        User user = User.createUser(3, "bob", "password", "student", "Bob Johnson", "bob@test.com", null);
        
        // Execute - Update the answer
        Result updateResult = answers.update(answerId, questionId, user, "Updated content", true);
        
        // Verify - Check update was successful
        if (!updateResult.isSuccess()) {
            System.out.println("  Error: Answer update failed - " + updateResult.getMessage());
            db.closeConnection();
            return false;
        }
        
        Answer updatedAnswer = answers.read(answerId);
        if (!updatedAnswer.getContent().equals("Updated content")) {
            System.out.println("  Error: Content was not updated");
            db.closeConnection();
            return false;
        }
        
        if (!updatedAnswer.isSolution()) {
            System.out.println("  Error: Solution flag was not updated");
            db.closeConnection();
            return false;
        }
        
        System.out.println("  Answer updated successfully");
        db.closeConnection();
        return true;
    }
    
    /**
     * Test 4: Delete Answer Operation
     * 
     * <p>Verifies that an answer can be successfully deleted from the system.
     * Tests the delete operation by creating an answer and then removing it.
     * 
     * <p>Test Steps:
     * <ol>
     *   <li>Connect to database and ensure test question exists</li>
     *   <li>Create a new answer</li>
     *   <li>Record the total number of answers</li>
     *   <li>Create a user with delete permissions</li>
     *   <li>Delete the answer</li>
     *   <li>Verify the deletion was successful</li>
     *   <li>Verify the answer count decreased by one</li>
     *   <li>Verify the answer can no longer be retrieved</li>
     * </ol>
     * 
     * @return true if the test passes, false otherwise
     */
    public static boolean testDeleteAnswer() {
        System.out.println("  Testing answer deletion operation...");
        
        // Setup - Create DatabaseHelper
        DatabaseHelper db = new DatabaseHelper();
        try {
            db.connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            db.closeConnection();
            return false;
        }
        
        // Get a valid question ID from existing questions
        int questionId = getValidQuestionId(db);
        if (questionId < 0) {
            System.out.println("  Error: Could not find valid question");
            db.closeConnection();
            return false;
        }
        
        // Create Answers instance AFTER question exists
        Answers answers = new Answers(db);
        Result createResult = answers.create(4, questionId, "Alice Williams", "Answer to be deleted");
        
        if (!createResult.isSuccess()) {
            System.out.println("  Error: Failed to create test answer");
            db.closeConnection();
            return false;
        }
        
        Answer createdAnswer = (Answer) createResult.getData();
        int answerId = createdAnswer.getAnswerId();
        int sizeBeforeDelete = answers.size();
        
        // Create a user who is the author
        User user = User.createUser(4, "alice", "password", "student", "Alice Williams", "alice@test.com", null);
        
        // Execute - Delete the answer
        Result deleteResult = answers.delete(answerId, user);
        
        // Verify - Check deletion was successful
        if (!deleteResult.isSuccess()) {
            System.out.println("  Error: Answer deletion failed - " + deleteResult.getMessage());
            db.closeConnection();
            return false;
        }
        
        if (answers.size() != sizeBeforeDelete - 1) {
            System.out.println("  Error: Answer count did not decrease");
            db.closeConnection();
            return false;
        }
        
        Answer deletedAnswer = answers.read(answerId);
        if (deletedAnswer != null) {
            System.out.println("  Error: Answer still exists after deletion");
            db.closeConnection();
            return false;
        }
        
        System.out.println("  Answer deleted successfully");
        db.closeConnection();
        return true;
    }
    
    /**
     * Test 5: Search Answers Operation
     * 
     * <p>Verifies that answers can be searched using various criteria.
     * Tests the search operation with keyword filtering to find specific answers.
     * 
     * <p>Test Steps:
     * <ol>
     *   <li>Connect to database and ensure test question exists</li>
     *   <li>Create multiple answers with different content</li>
     *   <li>Search for answers containing a specific keyword</li>
     *   <li>Verify search results are not null</li>
     *   <li>Verify search returns at least one match</li>
     *   <li>Verify at least one result contains the search keyword</li>
     * </ol>
     * 
     * @return true if the test passes, false otherwise
     */
    public static boolean testSearchAnswers() {
        System.out.println("  Testing answer search operation...");
        
        // Setup - Create DatabaseHelper and test answers
        DatabaseHelper db = new DatabaseHelper();
        try {
            db.connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            db.closeConnection();
            return false;
        }
        
        // Get a valid question ID from existing questions
        int questionId = getValidQuestionId(db);
        if (questionId < 0) {
            System.out.println("  Error: Could not find valid question");
            db.closeConnection();
            return false;
        }
        
        Answers answers = new Answers(db);
        answers.create(5, questionId, "Charlie Brown", "This answer discusses Java programming");
        answers.create(5, questionId, "Charlie Brown", "This answer is about Python programming");
        answers.create(5, questionId, "Charlie Brown", "This answer covers database design");
        
        // Execute - Search for answers containing "Java"
        List<Answer> searchResults = answers.search("Java", null, null);
        
        // Verify - Check search results
        if (searchResults == null) {
            System.out.println("  Error: Search results are null");
            db.closeConnection();
            return false;
        }
        
        if (searchResults.isEmpty()) {
            System.out.println("  Error: Search returned no results");
            db.closeConnection();
            return false;
        }
        
        // Verify at least one result contains the keyword
        boolean foundMatch = false;
        for (Answer answer : searchResults) {
            if (answer.getContent().toLowerCase().contains("java")) {
                foundMatch = true;
                break;
            }
        }
        
        if (!foundMatch) {
            System.out.println("  Error: No search results contain the keyword 'Java'");
            db.closeConnection();
            return false;
        }
        
        System.out.println("  Search found " + searchResults.size() + " matching answer(s)");
        db.closeConnection();
        return true;
    }
    
    /**
     * Functional interface for test methods.
     * Allows tests to be passed as method references to the runTest helper.
     */
    @FunctionalInterface
    interface TestFunction {
        /**
         * Executes a test method.
         * 
         * @return true if test passes, false if it fails
         */
        boolean execute();
    }
}