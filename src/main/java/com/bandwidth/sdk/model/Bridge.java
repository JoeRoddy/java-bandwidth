package com.bandwidth.sdk.model;

import com.bandwidth.sdk.BandwidthConstants;
import com.bandwidth.sdk.BandwidthRestClient;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author vpotapenko
 */
public class Bridge {

    private final BandwidthRestClient client;

    private String id;
    private BridgeState state;
    private String[] callIds;
    private String calls;
    private boolean bridgeAudio;
    private Date completedTime;
    private Date createdTime;
    private Date activatedTime;

    private Bridge(BandwidthRestClient client) {
        this.client = client;
    }

    public static Bridge from(BandwidthRestClient client, JSONObject jsonObject) {
        Bridge bridge = new Bridge(client);
        bridge.id = (String) jsonObject.get("id");
        bridge.state = BridgeState.from((String) jsonObject.get("state"));
        bridge.calls = (String) jsonObject.get("calls");
        bridge.bridgeAudio = jsonObject.get("bridgeAudio").equals("true");

        if (jsonObject.containsKey("callIds")) {
            JSONArray jsonArray = (JSONArray) jsonObject.get("callIds");
            bridge.callIds = new String[jsonArray.size()];
            for (int i = 0; i < bridge.callIds.length; i++) {
                bridge.callIds[i] = (String) jsonArray.get(i);
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(BandwidthConstants.TRANSACTION_DATE_TIME_PATTERN);
        try {
            String time = (String) jsonObject.get("completedTime");
            if (StringUtils.isNotEmpty(time)) bridge.completedTime = dateFormat.parse(time);

            time = (String) jsonObject.get("createdTime");
            if (StringUtils.isNotEmpty(time)) bridge.createdTime = dateFormat.parse(time);

            time = (String) jsonObject.get("activatedTime");
            if (StringUtils.isNotEmpty(time)) bridge.activatedTime = dateFormat.parse(time);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return bridge;
    }

    public List<Call> getBridgeCalls() throws IOException {
        JSONArray jsonArray = client.requestBridgeCalls(id);

        List<Call> callList = new ArrayList<Call>();
        for (Object obj : jsonArray) {
            callList.add(Call.from(client, (JSONObject) obj));
        }
        return callList;
    }

    public void setCallIds(String[] callIds) {
        this.callIds = callIds;
    }

    public void setBridgeAudio(boolean bridgeAudio) {
        this.bridgeAudio = bridgeAudio;
    }

    public void commit() throws IOException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("bridgeAudio", String.valueOf(bridgeAudio));
        params.put("callIds", callIds == null ? Collections.emptyList() : Arrays.asList(callIds));

        client.updateBridge(id, params);
    }

    public String getId() {
        return id;
    }

    public BridgeState getState() {
        return state;
    }

    public String[] getCallIds() {
        return callIds;
    }

    public String getCalls() {
        return calls;
    }

    public boolean isBridgeAudio() {
        return bridgeAudio;
    }

    public Date getCompletedTime() {
        return completedTime;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public Date getActivatedTime() {
        return activatedTime;
    }

    public NewBridgeAudioBuilder newBridgeAudioBuilder() {
        return new NewBridgeAudioBuilder();
    }

    public void stopAudioFilePlaying() throws IOException {
        new NewBridgeAudioBuilder().fileUrl(StringUtils.EMPTY).create();
    }

    public void stopSentence() throws IOException {
        new NewBridgeAudioBuilder().sentence(StringUtils.EMPTY).create();
    }

    private void createAudio(Map<String, Object> params) throws IOException {
        client.createBridgeAudio(getId(), params);
    }

    @Override
    public String toString() {
        return "Bridge{" +
                "id='" + id + '\'' +
                ", state=" + state +
                ", callIds=" + Arrays.toString(callIds) +
                ", calls='" + calls + '\'' +
                ", bridgeAudio=" + bridgeAudio +
                ", completedTime=" + completedTime +
                ", createdTime=" + createdTime +
                ", activatedTime=" + activatedTime +
                '}';
    }

    public class NewBridgeAudioBuilder {

        private final Map<String, Object> params = new HashMap<String, Object>();

        public NewBridgeAudioBuilder fileUrl(String fileUrl) {
            params.put("fileUrl", fileUrl);
            return this;
        }

        public NewBridgeAudioBuilder sentence(String sentence) {
            params.put("sentence", sentence);
            return this;
        }

        public NewBridgeAudioBuilder gender(Gender gender) {
            params.put("gender", gender.name());
            return this;
        }

        public NewBridgeAudioBuilder locale(SentenceLocale locale) {
            params.put("locale", locale.restValue);
            return this;
        }

        public NewBridgeAudioBuilder voice(String voice) {
            params.put("voice", voice);
            return this;
        }

        public NewBridgeAudioBuilder loopEnabled(boolean loopEnabled) {
            params.put("loopEnabled", String.valueOf(loopEnabled));
            return this;
        }

        public void create() throws IOException {
            createAudio(params);
        }
    }
}
