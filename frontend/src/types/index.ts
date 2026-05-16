export interface ApiResult<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// Business enums
export type HeatStatus = 0 | 1
export type QualityStatus = 0 | 1 | 2 | 3
export type ApproveStatus = 0 | 1 | 2
export type DocType = 1 | 2
export type StockStatus = 0 | 1 | 2
export type QcResult = 1 | 2 | 3
export type ReconStatus = 0 | 1 | 2
export type RiskLevel = 1 | 2 | 3 | 4
export type BreakType = 1 | 2 | 3 | 4 | 5 | 6
export type ComplaintStatus = 0 | 1 | 2 | 3 | 4 | 5

export interface Heat {
  id?: number
  heatId: string
  smeltDate: string
  steelGrade?: string
  cContent?: number
  siContent?: number
  mnContent?: number
  pContent?: number
  sContent?: number
  castStartTime?: string
  shiftGroup?: string
  equipmentId?: string
  status: HeatStatus
  abnormalDesc?: string
}

export interface Slab {
  id?: number
  slabId: string
  heatId: string
  specifications?: string
  weight?: number
  castShift?: string
  castTime?: string
  rollBatchId?: string
  qualityStatus: QualityStatus
}

export interface RollBatch {
  id?: number
  batchId: string
  productionLine?: string
  workOrderId?: string
  rollDate?: string
  shiftGroup?: string
  status: number
  abnormalDesc?: string
}

export interface Coil {
  id?: number
  coilId: string
  specifications?: string
  weight?: number
  material?: string
  qualityGrade?: string
  batchId?: string
  inboundOrderNo?: string
  storageLocation?: string
  stockStatus: StockStatus
  outboundOrderNo?: string
  customerId?: string
  customerName?: string
  lifecycleStatus: number
}

export interface QcRecord {
  id?: number
  relateType: number
  relateId: string
  inspectItem?: string
  inspectValue?: string
  standardValue?: string
  result: QcResult
  inspectTime?: string
  inspector?: string
  failReason?: string
  disposeMethod?: string
}

export interface WorkReport {
  id?: number
  workOrderId: string
  processName?: string
  batchId?: string
  coilId?: string
  reportQuantity?: number
  reportTime?: string
  operator?: string
  shiftGroup?: string
  approveStatus: ApproveStatus
}

export interface Inventory {
  id?: number
  docNo: string
  docType: DocType
  batchId?: string
  coilId?: string
  quantity?: number
  operateTime?: string
  warehouse?: string
  operator?: string
  status: number
}

export interface Stock {
  id?: number
  coilId: string
  warehouse?: string
  location?: string
  quantity?: number
  stockStatus: StockStatus
  version?: number
}

export interface ReconDiff {
  id?: number
  batchId?: string
  coilId?: string
  workReportQty?: number
  qcPassQty?: number
  erpInboundQty?: number
  stockQty?: number
  diffType: number
  description?: string
  responsibleDept?: string
  status: ReconStatus
}

export interface ChainBreak {
  id?: number
  breakType: BreakType
  heatId?: string
  slabId?: string
  batchId?: string
  coilId?: string
  breakDesc?: string
  riskLevel: RiskLevel
  responsibleDept?: string
  status: ReconStatus
}

export interface Complaint {
  id?: number
  complaintId: string
  customerId?: string
  customerName?: string
  coilId?: string
  problemDesc?: string
  severity?: number
  responsibleDept?: string
  rootCause?: string
  correctiveMeasures?: string
  rectificationResult?: string
  reviewOpinion?: string
  status: ComplaintStatus
}

export interface TraceNode {
  type: string
  id: string
  data: any
  status: string
}

export interface TraceEdge {
  from: string
  to: string
  relation: string
}

export interface TraceResult {
  inputValue: string
  nodes: TraceNode[]
  edges: TraceEdge[]
  isComplete?: boolean
  abnormalNodeCount: number
}

export interface DashboardVO {
  pendingComplaints: number
  pendingChainBreaks: number
  pendingReconDiffs: number
  warningBatches: number
  traceCount: number
  chainBreakRate: number
  reconDiffRate: number
  complaintCloseRate: number
  recentChainBreaks?: ChainBreak[]
  recentReconDiffs?: ReconDiff[]
  criticalBreaks?: number
}

export interface UserInfo {
  id: number
  username: string
  realName: string
  roles: string[]
}

export interface LoginResult {
  token: string
  refreshToken: string
  userInfo: UserInfo
}
