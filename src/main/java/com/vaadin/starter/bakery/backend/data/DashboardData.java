package com.vaadin.starter.bakery.backend.data;

import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.starter.bakery.backend.data.entity.Product;

/**
 * Classe que representa os dados do dashboard da aplicação Bakery.
 * <p>
 * Armazena estatísticas referentes a entregas e vendas de produtos,
 * incluindo informações mensais e anuais, bem como o total de entregas por produto.
 * </p>
 *
 * <ul>
 *   <li>{@link DeliveryStats} - Estatísticas gerais de entrega.</li>
 *   <li>{@code deliveriesThisMonth} - Lista com o número de entregas realizadas no mês atual.</li>
 *   <li>{@code deliveriesThisYear} - Lista com o número de entregas realizadas no ano atual.</li>
 *   <li>{@code salesPerMonth} - Matriz com o total de vendas por mês.</li>
 *   <li>{@code productDeliveries} - Mapa com a quantidade de entregas por produto.</li>
 * </ul>
 *
 * @author (Seu nome)
 * @since 1.0
 */
public class DashboardData {

    /**
     * Estatísticas gerais de entrega.
     */
    private DeliveryStats deliveryStats;

    /**
     * Lista contendo o número de entregas realizadas em cada dia do mês atual.
     */
    private List<Number> deliveriesThisMonth;

    /**
     * Lista contendo o número de entregas realizadas em cada mês do ano atual.
     */
    private List<Number> deliveriesThisYear;

    /**
     * Matriz representando o total de vendas por mês, onde cada linha corresponde a um mês.
     */
    private Number[][] salesPerMonth;

    /**
     * Mapa que associa um produto à quantidade de entregas realizadas para ele.
     */
    private LinkedHashMap<Product, Integer> productDeliveries;

    /**
     * Obtém as estatísticas gerais de entrega.
     *
     * @return estatísticas de entrega
     */
    public DeliveryStats getDeliveryStats() {
        return deliveryStats;
    }

    /**
     * Define as estatísticas gerais de entrega.
     *
     * @param deliveryStats estatísticas de entrega a serem definidas
     */
    public void setDeliveryStats(DeliveryStats deliveryStats) {
        this.deliveryStats = deliveryStats;
    }

    /**
     * Obtém a lista de entregas realizadas no mês atual.
     *
     * @return lista de entregas deste mês
     */
    public List<Number> getDeliveriesThisMonth() {
        return deliveriesThisMonth;
    }

    /**
     * Define a lista de entregas realizadas no mês atual.
     *
     * @param deliveriesThisMonth lista de entregas deste mês
     */
    public void setDeliveriesThisMonth(List<Number> deliveriesThisMonth) {
        this.deliveriesThisMonth = deliveriesThisMonth;
    }

    /**
     * Obtém a lista de entregas realizadas no ano atual.
     *
     * @return lista de entregas deste ano
     */
    public List<Number> getDeliveriesThisYear() {
        return deliveriesThisYear;
    }

    /**
     * Define a lista de entregas realizadas no ano atual.
     *
     * @param deliveriesThisYear lista de entregas deste ano
     */
    public void setDeliveriesThisYear(List<Number> deliveriesThisYear) {
        this.deliveriesThisYear = deliveriesThisYear;
    }

    /**
     * Define a matriz de vendas por mês.
     *
     * @param salesPerMonth matriz de vendas por mês
     */
    public void setSalesPerMonth(Number[][] salesPerMonth) {
        this.salesPerMonth = salesPerMonth;
    }

    /**
     * Obtém o vetor de vendas do mês especificado.
     *
     * @param i índice do mês (0 para janeiro, 1 para fevereiro, etc.)
     * @return vetor de vendas para o mês
     */
    public Number[] getSalesPerMonth(int i) {
        return salesPerMonth[i];
    }

    /**
     * Obtém o mapa de entregas por produto.
     *
     * @return mapa de produtos e suas quantidades de entrega
     */
    public LinkedHashMap<Product, Integer> getProductDeliveries() {
        return productDeliveries;
    }

    /**
     * Define o mapa de entregas por produto.
     *
     * @param productDeliveries mapa de produtos e suas quantidades de entrega
     */
    public void setProductDeliveries(LinkedHashMap<Product, Integer> productDeliveries) {
        this.productDeliveries = productDeliveries;
    }

}