[*][]{anything}={anything}
[consequence][]ineligible because of {condition}=Exclusion e = new Exclusion(j, w);  e.setType(Exclusion.Type.{condition});insertLogical(e);
[condition][]time matters=ds: DispatchState()
[condition][]job dispatchable=not JobExclusion(jobId == j.jobId)
[condition][]within range=dist_mr: DecimalInfo(jobId == j.jobId, workerId == w.workerId, type == Info.Type.DISTANCE_TO_JOB_MILES);MaxRadius(jobId == j.jobId, maxRadius >= dist_mr.value)
