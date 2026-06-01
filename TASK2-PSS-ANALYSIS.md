# TASK 2 - Pod Security Standards (PSS) Analysis

## 2a) Analyse écrite Baseline PSS

### Configuration appliquée: Baseline

Le profil **Baseline** représente le niveau minimal de sécurité pour les pods, permettant une large compatibilité.

#### Restrictions Baseline:
- ✅ Privilèges: `privileged=false` (pas de conteneurs privilégiés)
- ✅ Capability: CANNOT avoir `SYS_ADMIN`, `NET_ADMIN`, etc.
- ✅ User: Pas de restrictions spéciales
- ✅ SELinux: Pas de politique restrictive
- ✅ Volume: Tous les types autorisés

#### Résumé pour foodndeliv:
- **API RW & API RO**: Utilisant readOnlyRootFilesystem=true et capabilities.drop=["ALL"]
- **Keycloak**: Exécuté sans restrictions de sécurité renforcées
- **CNPG**: Déployé avec configuration standard PostgreSQL
- **Impact**: Configuration compatible mais **NON optimale** pour la sécurité

---

## 2b) Analyse écrite Restricted PSS

### Configuration cible: Restricted

Le profil **Restricted** représente les meilleures pratiques de sécurité imposant des contraintes strictes.

#### Restrictions Restricted:
- ✅ runAsNonRoot=true (OBLIGATOIRE)
- ✅ capabilities.drop=["ALL"] (OBLIGATOIRE)  
- ✅ readOnlyRootFilesystem=true (FORTEMENT recommandé)
- ✅ seccompProfile.type=RuntimeDefault (OBLIGATOIRE)
- ✅ allowPrivilegeEscalation=false (OBLIGATOIRE)
- ❌ Volume hostPath/hostNetwork INTERDITS

#### Modifications requises pour foodndeliv:

**API RW & API RO**: ✅ DÉJÀ CONFORMES
```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1000
  readOnlyRootFilesystem: true
  allowPrivilegeEscalation: false
  capabilities:
    drop: ["ALL"]
  seccompProfile:
    type: RuntimeDefault
```

**Keycloak**: ❌ À CORRIGER
```yaml
# AJOUTER:
securityContext:
  runAsNonRoot: true
  runAsUser: 1000
  allowPrivilegeEscalation: false
  capabilities:
    drop: ["ALL"]
  seccompProfile:
    type: RuntimeDefault
# readOnlyRootFilesystem: true serait incompatible avec Keycloak
```

**CNPG**: ✅ Généralement conforme (géré par l'opérateur)

---

## 2c) kubectl dry-run PSS - Baseline vs Restricted

### Commande 1: Dry-run avec Baseline Policy
