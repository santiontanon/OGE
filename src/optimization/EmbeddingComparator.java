/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import orthographicembedding.OrthographicEmbeddingResult;

/**
 *
 * @author santi
 */
public interface EmbeddingComparator {
    public int compare(OrthographicEmbeddingResult oer1, OrthographicEmbeddingResult oer2);
}
