/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ui.loaders;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import javax.validation.constraints.NotNull;

/**
 * Implementation of PopupLoader.
 * This loader is UI widget appearing on the top of the IDE.
 *
 * @author Vitaliy Guliy
 */
public class PopupLoaderImpl extends Composite implements PopupLoader {

    interface LoaderPopupImplUiBinder extends UiBinder<FlowPanel, PopupLoaderImpl> {
    }

    // Loader title
    String title;

    // Loader title animation suffix
    String titleSuffix = "";

    // Loader description
    String description;

    @UiField
    Label titleLabel;

    @UiField
    Label descriptionLabel;

    @Inject
    public PopupLoaderImpl(LoaderPopupImplUiBinder uiBinder,
                           @NotNull @Assisted("title") String title,
                           @NotNull @Assisted("description") String description) {
        initWidget(uiBinder.createAndBindUi(this));

        this.title = title;
        this.description = description;

        titleLabel.setText(title);
        descriptionLabel.setText(description);

        // Start show animation
        getElement().addClassName("inDown");

        // Attach to the root
        RootPanel.get().add(this);

        // Start animation timer
        playTimer.scheduleRepeating(1000);
    }

    @Override
    public void setSuccess() {
        // Stop animation timer
        playTimer.cancel();

        // Start hide animation
        getElement().addClassName("outDown");

        // Remove from the parent
        new Timer() {
            @Override
            public void run() {
                removeFromParent();
            }
        }.schedule(1000);
    }

    @Override
    public void setError() {
        // Stop animation
        playTimer.cancel();

        // Reset timer
        titleLabel.setText(title);
    }

    private Timer playTimer = new Timer() {
        @Override
        public void run() {
            titleSuffix += ".";
            if (titleSuffix.length() > 3) {
                titleSuffix = ".";
            }

            titleLabel.setText(title + titleSuffix);
        }
    };

}
