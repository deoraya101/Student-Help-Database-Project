package testing;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

class ShreyaTest {

    static class MockRequest {
        int requestId;
        String requester;
        String description;
        String status;
        String adminResponse;
        Timestamp requestDate; 
        Timestamp resolutionDate;

        MockRequest(int requestId, String requester, String description, 
                   String status, String adminResponse, 
                   Timestamp requestDate, Timestamp resolutionDate) {
            this.requestId = requestId;
            this.requester = requester;
            this.description = description;
            this.status = status;
            this.adminResponse = adminResponse;
            this.requestDate = requestDate;
            this.resolutionDate = resolutionDate;
        }

        public String getStatus() { return status; }
        public String getAdminResponse() { return adminResponse; }
        public Timestamp getResolutionDate() { return resolutionDate; }
    }

    static List<MockRequest> mockRequestDatabase = new ArrayList<>();

    // Test 1: Instructor can submit admin requests
    @Test
    void testInstructorCanSubmitRequest() {
        MockRequest request = new MockRequest(
            1001, 
            "instructor1", 
            "Need access to course materials", 
            "Open", 
            null,
            Timestamp.from(Instant.now()), 
            null
        );
        mockRequestDatabase.add(request);

        MockRequest latest = mockRequestDatabase.get(mockRequestDatabase.size() - 1);
        assertEquals("instructor1", latest.requester);
        assertEquals("Open", latest.getStatus());
        assertNull(latest.getAdminResponse());
    }

    // Test 2: Admin can close requests
    @Test
    void testAdminCanCloseRequest() {
        MockRequest request = new MockRequest(
            1002,
            "instructor2",
            "Lab equipment needed",
            "Open",
            null,
            Timestamp.from(Instant.now()),
            null
        );
        mockRequestDatabase.add(request);

        // Admin closes the request
        for (MockRequest req : mockRequestDatabase) {
            if (req.requestId == 1002) {
                req.status = "Closed";
                req.adminResponse = "Equipment ordered";
                req.resolutionDate = Timestamp.from(Instant.now());
                break;
            }
        }

        MockRequest updated = mockRequestDatabase.get(mockRequestDatabase.size() - 1);
        assertEquals("Closed", updated.getStatus());
        assertEquals("Equipment ordered", updated.getAdminResponse());
        assertNotNull(updated.getResolutionDate());
    }

    // Test 3: Admin can reopen closed requests
    @Test
    void testAdminCanReopenRequest() {
        MockRequest request = new MockRequest(
            1003,
            "staff1",
            "Software license renewal",
            "Closed",
            "Completed",
            Timestamp.from(Instant.now()),
            Timestamp.from(Instant.now())
        );
        mockRequestDatabase.add(request);

        // Admin reopens the request
        for (MockRequest req : mockRequestDatabase) {
            if (req.requestId == 1003) {
                req.status = "Open";
                req.resolutionDate = null;
                break;
            }
        }

        MockRequest updated = mockRequestDatabase.get(mockRequestDatabase.size() - 1);
        assertEquals("Open", updated.getStatus());
        assertNull(updated.getResolutionDate());
    }

    // Test 4: Instructor can delete their own requests
    @Test
    void testInstructorCanDeleteOwnRequest() {
        MockRequest request = new MockRequest(
            1004,
            "instructor3",
            "Classroom change",
            "Open",
            null,
            Timestamp.from(Instant.now()),
            null
        );
        mockRequestDatabase.add(request);

        int initialSize = mockRequestDatabase.size();
        
        // Instructor deletes their request
        mockRequestDatabase.removeIf(req -> 
            req.requestId == 1004 && req.requester.equals("instructor3"));
        
        assertEquals(initialSize - 1, mockRequestDatabase.size());
    }

    // Test 5: Validate request timestamps
    @Test
    void testRequestTimestamps() {
        MockRequest request = new MockRequest(
            1005,
            "staff2",
            "Network issues",
            "Open",
            null,
            Timestamp.from(Instant.now()),
            null
        );
        mockRequestDatabase.add(request);

        MockRequest latest = mockRequestDatabase.get(mockRequestDatabase.size() - 1);
        long now = Instant.now().toEpochMilli();
        long requestTime = latest.requestDate.getTime();
        assertTrue(now - requestTime < 1000); // Created within last second
    }
}