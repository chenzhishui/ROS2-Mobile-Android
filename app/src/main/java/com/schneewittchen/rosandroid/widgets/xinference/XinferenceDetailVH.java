package com.schneewittchen.rosandroid.widgets.xinference;


import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.schneewittchen.rosandroid.R;
import com.schneewittchen.rosandroid.model.entities.widgets.BaseEntity;
import com.schneewittchen.rosandroid.ui.views.details.SubscriberWidgetViewHolder;
import com.schneewittchen.rosandroid.utility.Utils;

import java.util.Collections;
import java.util.List;

/**
 * Detail view holder for Xinference widget configuration.
 *
 * @author Copilot
 * @version 1.0.0
 * @created on 18.12.2024
 */
public class XinferenceDetailVH extends SubscriberWidgetViewHolder {

    private Spinner rotationSpinner;
    private ArrayAdapter<CharSequence> rotationAdapter;


    @Override
    public void initView(View view) {
        rotationSpinner = view.findViewById(R.id.xinferenceRotation);

        // Init spinner
        rotationAdapter = ArrayAdapter.createFromResource(view.getContext(),
                R.array.button_rotation, android.R.layout.simple_spinner_dropdown_item);

        rotationSpinner.setAdapter(rotationAdapter);
    }

    @Override
    protected void bindEntity(BaseEntity entity) {
        XinferenceEntity xinferenceEntity = (XinferenceEntity) entity;
        int position = rotationAdapter.getPosition(Utils.numberToDegrees(xinferenceEntity.rotation));

        rotationSpinner.setSelection(position);
    }

    @Override
    protected void updateEntity(BaseEntity entity) {
        int rotation = Utils.degreesToNumber(rotationSpinner.getSelectedItem().toString());

        ((XinferenceEntity) entity).rotation = rotation;
    }

    @Override
    public List<String> getTopicTypes() {
        return Collections.singletonList(std_msgs.msg.String.class.getCanonicalName());
    }

}
