package com.schneewittchen.rosandroid.widgets.xinference;


import com.schneewittchen.rosandroid.model.entities.widgets.SubscriberWidgetEntity;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.Topic;


/**
 * Entity for Xinference widget that displays AI model inference results.
 *
 * @author Copilot
 * @version 1.0.0
 * @created on 18.12.2024
 */
public class XinferenceEntity extends SubscriberWidgetEntity {

    public String displayText;
    public int rotation;


    public XinferenceEntity() {
        this.width = 3;
        this.height = 2;
        this.topic = new Topic("xinference", std_msgs.msg.String.class.getCanonicalName());
        this.displayText = "Waiting for inference...";
        this.rotation = 0;
    }

}
