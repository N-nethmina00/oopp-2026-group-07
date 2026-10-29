import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

public class BMIcalculator extends JFrame {
    private JPanel contentPane;
    private JLabel titlelable;
    private JLabel unitsystem;
    private JRadioButton metricKgMetersRadioButton;
    private JRadioButton radioButton2;
    private JLabel detail;
    private JLabel weight;
    private JTextField textField1;
    private JLabel hight;
    private JTextField textField2;
    private JButton calculateButton;
    private JButton button2;
    private JLabel rlable;
    private JLabel bmilable;
    private JLabel valuelable;
    private JLabel clable;
    private JLabel categoryvaluelable;

    public BMIcalculator() {
        setContentPane(contentPane);
        setTitle("BMI Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        applyCustomStyling();

        pack();
        setLocationRelativeTo(null);

        ButtonGroup unitGroup = new ButtonGroup();
        unitGroup.add(metricKgMetersRadioButton);
        unitGroup.add(radioButton2);

        ActionListener unitToggleListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (metricKgMetersRadioButton.isSelected()) {
                    weight.setText("Weight (kg):");
                    hight.setText("Height (meters):");
                } else if (radioButton2.isSelected()) {
                    weight.setText("Weight (lbs):");
                    hight.setText("Height (inches):");
                }
            }
        };
        metricKgMetersRadioButton.addActionListener(unitToggleListener);
        radioButton2.addActionListener(unitToggleListener);

        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateBMI();
            }
        });

        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });
    }


    private void applyCustomStyling() {

        Color backgroundColor = new Color(248, 249, 250);
        Color primaryBlue = new Color(13, 110, 253);
        Color secondaryGray = new Color(108, 117, 125);
        Color darkTextColor = new Color(33, 37, 41);
        Color resultColor = new Color(25, 135, 84);


        contentPane.setBackground(backgroundColor);


        if (titlelable != null) {
            titlelable.setFont(new Font("Segoe UI", Font.BOLD, 20));
            titlelable.setForeground(primaryBlue);
        }


        Font sectionFont = new Font("Segoe UI", Font.BOLD, 13);
        if (unitsystem != null) { unitsystem.setFont(sectionFont); unitsystem.setForeground(darkTextColor); }
        if (detail != null) { detail.setFont(sectionFont); detail.setForeground(darkTextColor); }
        if (rlable != null) { rlable.setFont(sectionFont); rlable.setForeground(darkTextColor); }


        if (metricKgMetersRadioButton != null) metricKgMetersRadioButton.setBackground(backgroundColor);
        if (radioButton2 != null) radioButton2.setBackground(backgroundColor);


        if (calculateButton != null) {
            calculateButton.setBackground(primaryBlue);
            calculateButton.setForeground(Color.WHITE);
            calculateButton.setFocusPainted(false);
            calculateButton.setBorderPainted(false);
            calculateButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        }


        if (button2 != null) {
            button2.setBackground(secondaryGray);
            button2.setForeground(Color.WHITE);
            button2.setFocusPainted(false);
            button2.setBorderPainted(false);
            button2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        }


        if (valuelable != null) {
            valuelable.setFont(new Font("Segoe UI", Font.BOLD, 16));
            valuelable.setForeground(resultColor);
        }
        if (categoryvaluelable != null) {
            categoryvaluelable.setFont(new Font("Segoe UI", Font.BOLD, 16));
            categoryvaluelable.setForeground(resultColor);
        }
    }

    private void calculateBMI() {
        try {
            double weightValue = Double.parseDouble(textField1.getText().trim());
            double heightValue = Double.parseDouble(textField2.getText().trim());

            if (weightValue <= 0 || heightValue <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter positive numbers greater than 0.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double bmi = 0.0;

            if (metricKgMetersRadioButton.isSelected()) {
                bmi = weightValue / (heightValue * heightValue);
            } else if (radioButton2.isSelected()) {
                bmi = 703 * (weightValue / (heightValue * heightValue));
            } else {
                JOptionPane.showMessageDialog(this, "Please select a unit system.", "Selection Missing", JOptionPane.WARNING_MESSAGE);
                return;
            }

            DecimalFormat df = new DecimalFormat("#.#");
            valuelable.setText(df.format(bmi));

            String category;
            if (bmi < 18.5) {
                category = "Underweight";
            } else if (bmi >= 18.5 && bmi <= 24.9) {
                category = "Normal";
            } else if (bmi >= 25 && bmi <= 29.9) {
                category = "Overweight";
            } else {
                category = "Obese";
            }
            categoryvaluelable.setText(category);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numerical values for Weight and Height.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        textField1.setText("");
        textField2.setText("");
        valuelable.setText("");
        categoryvaluelable.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}

                new BMIcalculator().setVisible(true);
            }
        });
    }
}
