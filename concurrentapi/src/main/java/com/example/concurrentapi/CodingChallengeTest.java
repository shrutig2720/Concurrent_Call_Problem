package com.example.concurrentapi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class is used for testing purposes. It calculates the maximum number of concurrent calls
 * for each customer on each day from a static JSON input.
 */
public class CodingChallengeTest {

    static class Call {
        int customerId;
        String callId;
        long startTimestamp;
        long endTimestamp;

        public Call(int customerId, String callId, long startTimestamp, long endTimestamp) {
            this.customerId = customerId;
            this.callId = callId;
            this.startTimestamp = startTimestamp;
            this.endTimestamp = endTimestamp;
        }
        public int getCustomerId() {
            return this.customerId;
        }
    
        public void setCustomerId(int customerId) {
            this.customerId = customerId;
        }
    
        public String getCallId() {
            return this.callId;
        }
    
        public void setCallId(String callId) {
            this.callId = callId;
        }
    
        public long getStartTimestamp() {
            return this.startTimestamp;
        }
    
        public void setStartTimestamp(long startTimestamp) {
            this.startTimestamp = startTimestamp;
        }
    
        public long getEndTimestamp() {
            return this.endTimestamp;
        }
    
        public void setEndTimestamp(long endTimestamp) {
            this.endTimestamp = endTimestamp;
        }
    
        public Call customerId(int customerId) {
            setCustomerId(customerId);
            return this;
        }
    
        public Call callId(String callId) {
            setCallId(callId);
            return this;
        }
    
        public Call startTimestamp(long startTimestamp) {
            setStartTimestamp(startTimestamp);
            return this;
        }
    
        public Call endTimestamp(long endTimestamp) {
            setEndTimestamp(endTimestamp);
            return this;
        }
    
    
        @Override
        public int hashCode() {
            return Objects.hash(customerId, callId, startTimestamp, endTimestamp);
        }
    
        @Override
        public String toString() {
            return "{" +
                " customerId='" + getCustomerId() + "'" +
                ", callId='" + getCallId() + "'" +
                ", startTimestamp='" + getStartTimestamp() + "'" +
                ", endTimestamp='" + getEndTimestamp() + "'" +
                "}";
        }
    }

    static class Result {
        int customerId;
        String date;
        int maxConcurrentCalls;
        long timestamp;
        List<String> callIds;

        public Result(int customerId, String date, int maxConcurrentCalls, long timestamp, List<String> callIds) {
            this.customerId = customerId;
            this.date = date;
            this.maxConcurrentCalls = maxConcurrentCalls;
            this.timestamp = timestamp;
            this.callIds = callIds;
        }

        public JSONObject toJSON() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("customerId", customerId);
            json.put("date", date);
            json.put("maxConcurrentCalls", maxConcurrentCalls);
            json.put("timestamp", timestamp);
            json.put("callIds", new JSONArray(callIds));
            return json;
        }

    }

    public static void main(String[] args) throws JSONException {
        String jsonData = "{\n" +
                "    \"callRecords\": [\n" +
                "        {\n" +
                "            \"customerId\": 49121,\n" +
                "            \"callId\": \"ffddeb95-75dc-4479-88b4-9d4f59b3f2b0\",\n" +
                "            \"startTimestamp\": 1705158000000,\n" +
                "            \"endTimestamp\": 1705165200000\n" +
                "        },\n" +
                "        {\n" +
                "            \"customerId\": 49121,\n" +
                "            \"callId\": \"6f1ae36b-46c9-4560-b05c-80022ed858d1\",\n" +
                "            \"startTimestamp\": 1705158000000,\n" +
                "            \"endTimestamp\": 1705165200000\n" +
                "        },\n" +
                "        {\n" +
                "            \"customerId\": 49121,\n" +
                "            \"callId\": \"341f575b-dfec-41e0-a050-745a2b9bc698\",\n" +
                "            \"startTimestamp\": 1704582600000,\n" +
                "            \"endTimestamp\": 1704764040000\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        // Parse JSON input
        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray callRecords = jsonObject.getJSONArray("callRecords");

        // Convert JSON to Call objects
        List<Call> calls = new ArrayList<>();
        for (int i = 0; i < callRecords.length(); i++) {
            JSONObject callObject = callRecords.getJSONObject(i);
            int customerId = callObject.getInt("customerId");
            String callId = callObject.getString("callId");
            long startTimestamp = callObject.getLong("startTimestamp");
            long endTimestamp = callObject.getLong("endTimestamp");
            calls.add(new Call(customerId, callId, startTimestamp, endTimestamp));
        }

        // Group calls by customer and date
        System.out.println("Calls size initially: " + calls.size());
        Map<Integer, Map<String, List<Call>>> groupedCalls = groupCallsByCustomerAndDate(calls);
        System.out.println(groupedCalls);
        // Calculate max concurrent calls
        List<Result> results = calculateMaxConcurrentCalls(groupedCalls);

        // Sort results by date for consistent output
        results.sort(Comparator.comparing((Result r) -> r.date));

        // Print results
        JSONObject output = new JSONObject();
        JSONArray resultsArray = new JSONArray();
        for (Result result : results) {
            resultsArray.put(result.toJSON());
        }
        output.put("results", resultsArray);

        System.out.println(output.toString(2));
    }

    private static Map<Integer, Map<String, List<Call>>> groupCallsByCustomerAndDate(List<Call> calls) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Map<Integer, Map<String, List<Call>>> groupedCalls = new HashMap<>();

        for (Call call : calls) {
            // Determine the date range for this call
            String startDate = dateFormat.format(new Date(call.startTimestamp));
            String endDate = dateFormat.format(new Date(call.endTimestamp));
            System.out.println(call);
            System.out.println("Start Time: " + startDate);
            System.out.println("End Time: " + endDate);


            // Add call for each day it spans
            for (String date = startDate; !date.equals(endDate); date = incrementDate(date)) {
                groupedCalls.computeIfAbsent(call.customerId, k -> new HashMap<>())
                        .computeIfAbsent(date, k -> new ArrayList<>())
                        .add(call);
            }
            // Ensure the call is added for the end date as well
            groupedCalls.computeIfAbsent(call.customerId, k -> new HashMap<>())
                    .computeIfAbsent(endDate, k -> new ArrayList<>())
                    .add(call);
        }

        return groupedCalls;
    }

    // Increment the date by one day
    private static String incrementDate(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date currentDate = dateFormat.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            return dateFormat.format(calendar.getTime());
        } catch (Exception e) {
            throw new RuntimeException("Failed to increment date: " + date, e);
        }
    }

    private static List<Result> calculateMaxConcurrentCalls(Map<Integer, Map<String, List<Call>>> groupedCalls) {
        List<Result> results = new ArrayList<>();

        for (int customerId : groupedCalls.keySet()) {
            for (String date : groupedCalls.get(customerId).keySet()) {
                List<Call> calls = groupedCalls.get(customerId).get(date);

                // Find maximum concurrent calls
                int maxConcurrent = 0;
                long maxTimestamp = 0;
                List<String> maxConcurrentCallIds = new ArrayList<>();

                // Create events for each start and end of a call
                List<long[]> events = new ArrayList<>();
                for (Call call : calls) {
                    // Add event for start
                    long callStart = call.startTimestamp;
                    long callEnd = call.endTimestamp;
                    // Clamp events to the current date
                    long startOfDay = getStartOfDayInMillis(date);
                    long endOfDay = getEndOfDayInMillis(date);
                    if (callStart < startOfDay) callStart = startOfDay;
                    if (callEnd > endOfDay) callEnd = endOfDay;

                    events.add(new long[]{callStart, 1, calls.indexOf(call)});
                    events.add(new long[]{callEnd, -1, calls.indexOf(call)});
                }

                // Sort events by time; if times are equal, end event (-1) comes before start event (1)
                events.sort((e1, e2) -> {
                    if (e1[0] != e2[0]) return Long.compare(e1[0], e2[0]);
                    return Long.compare(e1[1], e2[1]);
                });

                // Track concurrent calls
                int currentConcurrent = 0;
                Map<String, Long> callIdToStartTime = new HashMap<>();
                Set<String> currentCallIds = new HashSet<>();

                for (long[] event : events) {
                    long timestamp = event[0];
                    int type = (int) event[1];
                    String callId = calls.get((int) event[2]).callId;
                    long callStart = calls.get((int) event[2]).startTimestamp;

                    // Update concurrent calls
                    if (type == 1) {
                        currentConcurrent++;
                        currentCallIds.add(callId);
                        callIdToStartTime.put(callId, callStart);
                    } else {
                        currentConcurrent--;
                        currentCallIds.remove(callId);
                    }

                    // Check if the current concurrency is the maximum
                    if (currentConcurrent > maxConcurrent) {
                        maxConcurrent = currentConcurrent;
                        maxTimestamp = timestamp;
                        maxConcurrentCallIds = new ArrayList<>(currentCallIds);
                    }
                }

                if (maxConcurrent > 0) { // Only add results if there were calls on that day
                    // Sort the call IDs by their original start timestamp to ensure consistent ordering
                    maxConcurrentCallIds.sort(Comparator.comparing(callIdToStartTime::get));
                    results.add(new Result(customerId, date, maxConcurrent, maxTimestamp, maxConcurrentCallIds));
                }
            }
        }

        return results;
    }

    private static long getStartOfDayInMillis(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date dayStart = dateFormat.parse(date);
            return dayStart.getTime();
        } catch (Exception e) {
            throw new RuntimeException("Error calculating start of day: " + date, e);
        }
    }

    private static long getEndOfDayInMillis(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date dayStart = dateFormat.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dayStart);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MILLISECOND, -1);
            return calendar.getTimeInMillis();
        } catch (Exception e) {
            throw new RuntimeException("Error calculating end of day: " + date, e);
        }
    }
}
