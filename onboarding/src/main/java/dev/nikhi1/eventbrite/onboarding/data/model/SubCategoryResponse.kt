package dev.nikhi1.eventbrite.onboarding.data.model

data class SubCategoryResponse(
    val pagination: Pagination,
    val subcategories: List<Subcategory>
) {
    companion object {
        val EMPTY = SubCategoryResponse(Pagination.EMPTY, emptyList())
    }
}