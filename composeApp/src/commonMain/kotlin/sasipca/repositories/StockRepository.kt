package sasipca.repositories

import sasipca.models.DeliveryCreationDTO
import sasipca.models.DeliveryUpdateDTO
import sasipca.models.DeliveryQueryDTO
import sasipca.models.Resposta
import sasipca.models.VDeliveryDTO
import sasipca.storage.ApiConfig
import sasipca.storage.authorizedRequest
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Repository responsável por gerir operações de stock e entregas.
 */
class StockRepository(private val client: HttpClient) {

    /**
     * Consulta entregas com filtros opcionais
     */
    suspend fun getDeliveries(query: DeliveryQueryDTO? = null): List<VDeliveryDTO> {

        return client.authorizedRequest<List<VDeliveryDTO>>(
            url = "${ApiConfig.baseUrl()}/stock/delivery",
            method = HttpMethod.Get
        ) {
            query?.let {
                parameter("StatusId", it.statusId)
                parameter("BeneficiaryId", it.beneficiaryId)
                parameter("DateFrom", it.dateFrom)
                parameter("DateTo", it.dateTo)
            }
        }

    }


    /**
     * Cria/Agenda uma nova entrega para uma data específica
     */
    suspend fun scheduleDelivery(dto: DeliveryCreationDTO): VDeliveryDTO {
        return client.authorizedRequest(
            url = "${ApiConfig.baseUrl()}/stock/delivery/schedule",
            method = HttpMethod.Post
        ) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }
    }


    /**
     * Atualiza uma entrega existente (ex: data, status, itens)
     * Apenas se estiver 'Agendada'
     */
    suspend fun updateDelivery(deliveryId: Int, dto: DeliveryUpdateDTO): VDeliveryDTO {
        return client.authorizedRequest<VDeliveryDTO>(
            url = "${ApiConfig.baseUrl()}/stock/delivery/$deliveryId",
            method = HttpMethod.Put
        ) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }
    }

    /**
     * Elimina uma entrega existinte (Apenas se tiver 'Agendada')
     */
    suspend fun deleteDelivery(deliveryId: Int): Resposta {
        return client.authorizedRequest<Resposta>(
            url = "${ApiConfig.baseUrl()}/stock/delivery/$deliveryId",
            method = HttpMethod.Delete
        ) {
            contentType(ContentType.Application.Json)
        }
    }


}
