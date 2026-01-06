package sasipca.storage

import sasipca.models.ActiveCampaigns
import sasipca.models.CategoryType
import sasipca.models.DeliveriesStatus
import sasipca.models.MovementType
import sasipca.models.ReportTypes
import sasipca.models.UnitTypeInfo

object ListsStore {

    var categoriestypes: List<CategoryType> = emptyList()
        private set

    var unitTypes: List<UnitTypeInfo> = emptyList()
        private set

    var movementTypes: List<MovementType> = emptyList()
    private set

    var DeliveriesStatus: List<DeliveriesStatus> = emptyList()
    private set

    var ReportTypes: List<ReportTypes> = emptyList()
        private set

    var ActiveCampaigns: List<ActiveCampaigns> = emptyList()
        private set

    fun load(categoriesTypes: List<CategoryType>,
             unitTypes: List<UnitTypeInfo>,
             movementTypes: List<MovementType>,
             deliveriesStatus: List<DeliveriesStatus>,
             reportTypes: List<ReportTypes>,
             activeCampaigns: List<ActiveCampaigns>

    ) {
        this.categoriestypes = categoriesTypes
        this.unitTypes = unitTypes
        this.movementTypes = movementTypes
        this.DeliveriesStatus = deliveriesStatus
        this.ReportTypes = reportTypes
        this.ActiveCampaigns = activeCampaigns
    }

    fun getCategoryName(id: Int): String =
        categoriestypes.firstOrNull { it.id == id }?.type ?: "Desconhecido"

    fun getDeliveriesStatusName(id: Int): String =
        DeliveriesStatus.firstOrNull { it.id == id }?.status ?: "Desconhecido"

    val isInitialized: Boolean
        get() = movementTypes.isNotEmpty() && unitTypes.isNotEmpty()

}
