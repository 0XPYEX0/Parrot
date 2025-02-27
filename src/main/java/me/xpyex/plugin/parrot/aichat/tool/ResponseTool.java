package me.xpyex.plugin.parrot.aichat.tool;

import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseTool {
    private String id;
    private String type = "function";
    private FunctionInfo function;

    @Getter
    @Setter
    public static class FunctionInfo {
        private String name;
        private String arguments;

        public JSONObject parseArguments() {
            try {
                return new JSONObject(arguments);
            } catch (JSONException e) {
                e.printStackTrace();
                return new JSONObject();
            }
        }
    }
}
