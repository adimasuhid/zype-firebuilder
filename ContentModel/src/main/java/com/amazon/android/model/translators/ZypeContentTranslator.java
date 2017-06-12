/**
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazon.android.model.translators;

import android.util.Log;

import com.amazon.android.model.AModelTranslator;
import com.amazon.android.model.content.Content;
import com.amazon.utils.ListUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/* Zype, Evgeny Cherkasov */

/**
 * This class extends the {@link AModelTranslator} for the {@link Content} class. It provides a way
 * to translate a {link Map} to a {@link Content} object.
 */
public class ZypeContentTranslator extends AModelTranslator<Content> {
    private static final String TAG = ZypeContentTranslator.class.getSimpleName();

    private static final String FIELD_SUBSCRIPTION_REQUIRED = "subscriptionRequired";

    /**
     * {@inheritDoc}
     *
     * @return A new {@link Content}
     */
    @Override
    public Content instantiateModel() {
        return new Content();
    }

    /**
     * Explicitly sets a member variable named field to the given value. If the field does not
     * match one of {@link Content}'s predefined field names, the field and value will be stored in
     * the {@link Content#mExtras} map.
     *
     * @param model The {@link Content} to set the field on.
     * @param field The {@link String} describing what member variable to set.
     * @param value The {@link Object} value to set the member variable.
     * @return True if the value was set, false if there was an error.
     */
    @Override
    public boolean setMemberVariable(Content model, String field, Object value) {
        if (model == null || field == null || field.isEmpty()) {
            Log.e(TAG, "Input parameters should not be null and field cannot be empty.");
            return false;
        }
        // This allows for some content to have extra values that others might not have.
        if (value == null) {
            Log.w(TAG, "Value for " + field + " was null so not set for Content, this may be " +
                    "intentional.");
            return true;
        }
        try {
            switch (field) {
                case Content.TITLE_FIELD_NAME:
                    model.setTitle(value.toString());
                    break;
                case Content.DESCRIPTION_FIELD_NAME:
                    model.setDescription(value.toString());
//                    if (TextUtils.isEmpty(model.getDescription())) {
//                        model.setDescription(" ");
//                    }
                    break;
                case Content.ID_FIELD_NAME:
                    model.setId(value.toString());
//                    // Convert Zype hex 24 digit id to long by getting hash code of id String
//                    // The result long id may be not unique.
//                    // TODO: Consider another way to convert original id to long value
//                    model.setId(Long.valueOf(String.valueOf(((String) value).hashCode())));
                    // TODO: Id type changes to String, so we probably don't need save original id anymore
                    // Put original id as extra value
                    model.setExtraValue("_id", value);
                    break;
                case Content.SUBTITLE_FIELD_NAME:
                    model.setSubtitle(value.toString());
                    break;
                case Content.URL_FIELD_NAME:
                    model.setUrl(value.toString());
                    break;
                case Content.CARD_IMAGE_URL_FIELD_NAME: {
                    model.setCardImageUrl(findImageUrl(field, value));
                    break;
                }
                case Content.BACKGROUND_IMAGE_URL_FIELD_NAME: {
                    model.setBackgroundImageUrl(findImageUrl(field, value));
                    break;
                }
                case Content.TAGS_FIELD_NAME:
                    // Expecting value to be a list.
                    model.setTags(value.toString());
                    break;
//                case FIELD_SUBSCRIPTION_REQUIRED: {
//                    model.setSubscriptionRequired((Boolean) value);
//                }
                case Content.CLOSED_CAPTION_FIELD_NAME:
                    model.setCloseCaptionUrls((List) value);
                    break;
                case Content.RECOMMENDATIONS_FIELD_NAME:
                    // Expecting value to be a list.
                    model.setRecommendations(value.toString());
                    break;
                case Content.AVAILABLE_DATE_FIELD_NAME:
                    model.setAvailableDate(value.toString());
                    break;
                case Content.SUBSCRIPTION_REQUIRED_FIELD_NAME:
                    model.setSubscriptionRequired((boolean) value);
                    break;
                case Content.CHANNEL_ID_FIELD_NAME:
                    model.setChannelId(value.toString());
                    break;
                case Content.DURATION_FIELD_NAME:
                    model.setDuration(Long.valueOf((String) value));
                    break;
                case Content.AD_CUE_POINTS_FIELD_NAME:
                    model.setAdCuePoints((List) value);
                    break;
                case Content.STUDIO_FIELD_NAME:
                    model.setStudio(value.toString());
                    break;
                case Content.FORMAT_FIELD_NAME:
                    model.setFormat(value.toString());
                    break;
                default:
                    model.setExtraValue(field, value);
                    break;
            }
        }
        catch (ClassCastException e) {
            Log.e(TAG, "Error casting value to the required type for field " + field, e);
            return false;
        }
        catch (ListUtils.ExpectingJsonArrayException e) {
            Log.e(TAG, "Error creating JSONArray from provided tags string " + value, e);
            return false;
        }
        return true;
    }

    /**
     * This method verifies that the {@link Content} model was properly translated and all the
     * mandatory fields were set. A valid {@link Content} model must have non-empty values for
     * {@link Content#mTitle}, {@link Content#mDescription}, {@link Content#mTags}, {@link
     * Content#mCardImageUrl},{@link Content#mBackgroundImageUrl}, and {@link Content#mUrl}. Note:
     * This method does not check the validity of the urls.
     *
     * @param model The {@link Content} model to verify.
     * @return True if the model is valid; false otherwise.
     */
    @Override
    public boolean validateModel(Content model) {
        try {
            return !model.getTitle().isEmpty() && !model.getDescription().isEmpty()
                    && !model.getUrl().isEmpty() && !model.getCardImageUrl().isEmpty()
                    && !model.getBackgroundImageUrl().isEmpty();
        }
        catch (NullPointerException e) {
            Log.e(TAG, "Null pointer found during model validation.", e);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return ZypeContentTranslator.class.getSimpleName();
    }

    private String findImageUrl(String field, Object value) {
        final int IMAGE_HEIGHT_CARD = 452;
        final int IMAGE_HEIGHT_BACKGROUND = 1080;

        String result = "null";
        int requiredImageHeight = 0;
        switch (field) {
            case Content.CARD_IMAGE_URL_FIELD_NAME: {
                requiredImageHeight = IMAGE_HEIGHT_CARD;
                break;
            }
            case Content.BACKGROUND_IMAGE_URL_FIELD_NAME: {
                requiredImageHeight = IMAGE_HEIGHT_BACKGROUND;
                break;
            }

        }
        try {
            JSONArray jsonValue = new JSONArray(value.toString());
            JSONObject jsonImage = null;
            for (int i = 0; i < jsonValue.length(); i++) {
                if (jsonImage == null) {
                    jsonImage = jsonValue.getJSONObject(i);
                }
                else {
                    JSONObject jsonObject = jsonValue.getJSONObject(i);
                    if (Math.abs(jsonObject.getInt("height") - requiredImageHeight) < Math.abs(jsonImage.getInt("height") - requiredImageHeight)) {
                        jsonImage = jsonObject;
                    }
                }
            }
            if (jsonImage != null) {
                result = jsonImage.getString("url");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
