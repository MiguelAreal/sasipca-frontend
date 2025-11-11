package sasipca.repositories

import sasipca.models.*
import sasipca.storage.ApiConfig
import sasipca.storage.requestWithAuth
import io.ktor.client.*
import io.ktor.http.*

class StockRepository(private val client: HttpClient) {

    /**
     * Consulta entregas com filtros opcionais
     */
    suspend fun getDeliveries(query: DeliveryQueryDTO? = null): List<VDeliveryDTO> {
        val url = buildString {
            append("${ApiConfig.baseUrl()}/stock/delivery")
            query?.let {
                append("?StatusId=${it.statusId}&BeneficiaryId=${it.beneficiaryId}&DateFrom=${it.dateFrom}&DateTo=${it.dateTo}")
            }
        }

        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = url
        )
    }

    /**
     * Cria/Agenda uma nova entrega para uma data específica
     */
    suspend fun scheduleDelivery(dto: DeliveryCreationDTO): VDeliveryDTO {
        return client.requestWithAuth(
            method = HttpMethod.Post,
            url = "${ApiConfig.baseUrl()}/stock/delivery/schedule",
            body = dto
        )
    }

    /**
     * Atualiza uma entrega existente (ex: data, status, itens)
     * Apenas se estiver 'Agendada'
     */
    suspend fun updateDelivery(deliveryId: Int, dto: DeliveryUpdateDTO): VDeliveryDTO {
        return client.requestWithAuth(
            method = HttpMethod.Put,
            url = "${ApiConfig.baseUrl()}/stock/delivery/$deliveryId",
            body = dto
        )
    }

    /**
     * Elimina uma entrega existente (Apenas se tiver 'Agendada')
     */
    suspend fun deleteDelivery(deliveryId: Int): Resposta {
        return client.requestWithAuth(
            method = HttpMethod.Delete,
            url = "${ApiConfig.baseUrl()}/stock/delivery/$deliveryId"
        )
    }
}
