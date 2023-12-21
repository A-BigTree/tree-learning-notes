import torch.nn as nn
import torch.nn.functional as F
import torch


class TransE(nn.Module):
    def __init__(self, entities_num, relations_num, entity_dimension, relation_dimension, margin, norm):
        super(TransE, self).__init__()
        self.entities_num = entities_num
        self.relations_num = relations_num

        self.margin = margin
        self.norm = norm

        tmp_entity_embeddings = ((6. / (entity_dimension ** 0.5) + 6. / (entity_dimension ** 0.5)) *
                                 torch.rand(self.entities_num, entity_dimension) - 6. / (entity_dimension ** 0.5))
        self.entity_embeddings = nn.Embedding.from_pretrained(tmp_entity_embeddings)

        tmp_relation_embeddings = F.normalize(tmp_entity_embeddings, 2, 1)
        self.relation_embeddings = nn.Embedding.from_pretrained(tmp_relation_embeddings)

    def forward(self, positive_sample, negative_sample, subsampling_weight, batch_type):
        pass
