package enums;

public enum ContributionStatus {
	DRAFT,
	NEW,
	PUBLISHED,
	EXCLUDED,
	ARCHIVED, 
	MODERATED, 
	INBALLOT,
	SELECTED,
	PUBLIC_DRAFT,
	FORKED_PRIVATE_DRAFT,
	FORKED_PUBLIC_DRAFT,
	FORKED_PUBLISHED, // Forked Published = Published Unmerged Amendment = an unmerged fork that is published by
	// fork authors. An unmerged fork may have been rejected by parent authors or simply ignored. In any case,
	// as we don’t have merge requests, fork authors may decide to publish their fork at any time.
	// In most cases, it signifies a dissensus proposal.
	MERGED_PRIVATE_DRAFT,
	MERGED_PUBLIC_DRAFT
}
