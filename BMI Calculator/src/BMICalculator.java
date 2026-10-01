import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BMICalculator {
    private JRadioButton metricRadio;
    private JRadioButton englishRadio;
    private JTextField textField1;
    private JTextField textField2;
    private JButton calculateButton;
    private JButton clearButton;
    private JTextArea BMIValuesUnderWeightLessTextArea;
    private JPanel mainPanel;
    private JLabel bmiOutputLabel;
    private JLabel categoryOutputLabel;
    private JLabel weightField;
    private JLabel heightField;

    public BMICalculator() {

        ButtonGroup unitGroup = new ButtonGroup();
        unitGroup.add(englishRadio);
        unitGroup.add(metricRadio);

        metricRadio.setSelected(true);
        updateLabels();

        metricRadio.addActionListener(e -> updateLabels());
        englishRadio.addActionListener(e -> updateLabels());

        calculateButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {

                try{

                    double weight = Double.parseDouble(textField1.getText().trim());
                    double height = Double.parseDouble(textField2.getText().trim());
                    double bmi = 0.0;

                    if(englishRadio.isSelected()){

                        bmi = (weight * 703) / (height * height);

                    }else{

                        bmi = weight / (height * height);

                    }

                    String category;

                    if(bmi < 18.5){
                        category = "Underweight";
                    } else if (bmi >= 18.5 && bmi <= 24.9){
                        category = "Normal";
                    } else if (bmi >= 25 && bmi <= 29.9){
                        category =  "Overweight";
                    } else {
                        category = "Obese";
                    }

                    bmiOutputLabel.setText(String.format("%.2f", bmi));
                    categoryOutputLabel.setText(category);


                }catch (NumberFormatException ex){

                    JOptionPane.showMessageDialog(mainPanel,
                            "Please enter valid numeric values for weight and height."
                    );

                }
            }

        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                weightField.setText("");
                heightField.setText("");
                bmiOutputLabel.setText("output");
                categoryOutputLabel.setText("output");
                metricRadio.setSelected(true);
                updateLabels();
            }
        });

    }

    private void updateLabels() {
        if (metricRadio.isSelected()) {
            weightField.setText("(Kg)");
            heightField.setText("(meters)");
        } else {
            weightField.setText("(lbs)");
            heightField.setText("(inches)");
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("BMI Calculator");
        BMICalculator calculatorForm = new BMICalculator();
        frame.setContentPane(calculatorForm.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}
