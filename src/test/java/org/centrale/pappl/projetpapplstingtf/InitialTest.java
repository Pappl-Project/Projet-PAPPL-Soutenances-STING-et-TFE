/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf;

/**
 *
 * @author srodr
 */
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

// Using AssertJ for clear validations
import static org.assertj.core.api.Assertions.assertThat;

class InitialTest {

    @Test
    @DisplayName("Grade Calculation: Should correctly calculate the average score")
    void testCalculateAverage() {
        // =================================================================
        // 1. GIVEN: The input scores for the project
        // =================================================================
        double defenseScore = 15.0; // Defense presentation
        double reportScore = 14.5;  // Written report
        double demoScore = 16.0;    // Technical demo

        // =================================================================
        // 2. WHEN: The logic is applied (Average calculation)
        // =================================================================
        // Logic: (A + B + C) / 3
        double finalGrade = (defenseScore + reportScore + demoScore) / 3;

        // =================================================================
        // 3. THEN: Assert the result is correct
        // =================================================================
        
        // The expected result is 15.1666... 
        // We verify that the result is close to 15.17 with a small margin of error (offset)
        assertThat(finalGrade).isCloseTo(15.16, org.assertj.core.data.Offset.offset(0.01))
            .as("The average grade should be accurate");
            
        // Check that the student passed (assuming pass mark is 10)
        assertThat(finalGrade).isGreaterThanOrEqualTo(10.0)
            .as("The student should pass the module");

        System.out.println("✅ Grade Test Passed: Final score is " + finalGrade);
    }
}