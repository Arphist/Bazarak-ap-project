package com.view;

import javafx.scene.control.ListCell;
import com.model.Advertisement;

public class AdCell extends ListCell<Advertisement> {
    @Override
    protected void updateItem(Advertisement ad, boolean empty) {
        super.updateItem(ad, empty);
        if (empty || ad == null) {
            setText(null);
        } else {
            setText(ad.getTitle() + "\n" + ad.getPrice() + " T");
        }
    }
}