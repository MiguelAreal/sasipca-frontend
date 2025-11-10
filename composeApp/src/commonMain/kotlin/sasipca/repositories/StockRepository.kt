package sasipca.repositories

import sasipca.models.*
import sasipca.storage.ApiConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class StockRepository(private val client: HttpClient) {

    /**
     * Consulta entregas com filtros opcionais
     */
    suspend fun getDeliveries(query: DeliveryQueryDTO? = null): List<VDeliveryDTO> {
        return client.get("${ApiConfig.baseUrl()}/stock/delivery") {
            query?.let {
                parameter("StatusId", it.statusId)
                parameter("BeneficiaryId", it.beneficiaryId)
                parameter("DateFrom", it.dateFrom)
                parameter("DateTo", it.dateTo)
            }
        }.body()
    }

    /**
     * Cria/Agenda uma nova entrega para uma data específica
     */
    suspend fun scheduleDelivery(dto: DeliveryCreationDTO): VDeliveryDTO {
        return client.post("${ApiConfig.baseUrl()}/stock/delivery/schedule") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    }

    /**
     * Atualiza uma entrega existente (ex: data, status, itens)
     * Apenas se estiver 'Agendada'
     */
    suspend fun updateDelivery(deliveryId: Int, dto: DeliveryUpdateDTO): VDeliveryDTO {
        return client.put("${ApiConfig.baseUrl()}/stock/delivery/$deliveryId") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    }

    /**
     * Elimina uma entrega existente (Apenas se tiver 'Agendada')
     */
    suspend fun deleteDelivery(deliveryId: Int): Resposta {
        return client.delete("${ApiConfig.baseUrl()}/stock/delivery/$deliveryId") {
            contentType(ContentType.Application.Json)
        }.body()
    }
}
