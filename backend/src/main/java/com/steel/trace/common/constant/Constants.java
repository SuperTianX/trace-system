package com.steel.trace.common.constant;

public class Constants {

    // 统一基础路径
    public static final String API_PREFIX = "/api/v1";

    // JWT
    public static final long JWT_EXPIRE_MS = 30 * 60 * 1000L;        // 30分钟
    public static final long JWT_REFRESH_EXPIRE_MS = 7 * 24 * 60 * 60 * 1000L; // 7天
    public static final String JWT_SECRET = "TraceSystemJwtSecretKey2026@Steel!";

    // 业务状态枚举
    public interface HeatStatus {
        int NORMAL = 0;
        int ABNORMAL = 1;
    }

    public interface QualityStatus {
        int PENDING = 0;
        int PASS = 1;
        int FAIL = 2;
        int RECHECK = 3;
    }

    public interface StockStatus {
        int IN_STOCK = 0;
        int OUTBOUND = 1;
        int LOCKED = 2;
    }

    public interface LifecycleStatus {
        int PRODUCTION = 0;
        int QC = 1;
        int INBOUND = 2;
        int OUTBOUND = 3;
        int COMPLAINT = 4;
    }

    public interface ApproveStatus {
        int PENDING = 0;
        int APPROVED = 1;
        int REJECTED = 2;
    }

    public interface InventoryDocType {
        int INBOUND = 1;
        int OUTBOUND = 2;
    }

    public interface DiffType {
        int QTY_MISMATCH = 1;
        int DATA_MISSING = 2;
    }

    public interface BreakType {
        int CHAIN_BREAK = 1;
        int QC_MISSING = 2;
        int UNQC_INBOUND = 3;
        int MES_ERP_MISMATCH = 4;
        int DUPLICATE_REPORT = 5;
        int QTY_ABNORMAL = 6;
    }

    public interface RiskLevel {
        int LOW = 1;
        int MEDIUM = 2;
        int HIGH = 3;
        int CRITICAL = 4;
    }

    public interface ComplaintStatus {
        int REGISTERED = 0;
        int TRACING = 1;
        int PROCESSING = 2;
        int RECHECKING = 3;
        int CLOSED = 4;
        int ARCHIVED = 5;
    }

    public interface Severity {
        int MINOR = 1;
        int MODERATE = 2;
        int SEVERE = 3;
        int CRITICAL = 4;
    }

    public interface ReconStatus {
        int UNPROCESSED = 0;
        int PROCESSING = 1;
        int CLOSED = 2;
    }

    public interface ChainBreakStatus {
        int PENDING = 0;
        int PROCESSING = 1;
        int CLOSED = 2;
    }

    public interface RelationType {
        int HEAT = 1;
        int SLAB = 2;
        int COIL = 3;
    }

    public interface TraceType {
        int FORWARD = 1;
        int BACKWARD = 2;
    }

    public interface RuleType {
        int TRACE = 1;
        int RECON = 2;
        int CHAIN = 3;
    }

    public interface QcResult {
        int PASS = 1;
        int FAIL = 2;
        int CONCESSION = 3;
    }
}
