/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;

/**
 *
 * @author lukas
 */
public class StepThroughButton extends Button {

    private int iterations;

    public StepThroughButton(String text, int iterations) {
        super(text);
        this.iterations = iterations;
    }

    @Override
    public void fire() {
        if (!isDisabled()) {
            fireEvent(new ActionEvent());
            iterations--;
        }
        if(iterations == 0){
            this.disableProperty().set(true);
        }
    }
}
