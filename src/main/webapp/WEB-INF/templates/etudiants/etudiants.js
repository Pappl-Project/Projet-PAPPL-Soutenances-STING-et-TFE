(function () {
  if (typeof jQuery === 'undefined') {
    console.error('jQuery absent'); return;
  }
  if (!$.fn || typeof $.fn.DataTable === 'undefined') {
    console.error('DataTables absent'); return;
  }
  if (typeof AppConfig === 'undefined') {
    console.error('AppConfig (contenant les URLs) est absent.'); return;
  }

  // État local
  let currentId   = null;
  let currentData = null; // dernier JSON full reçu
  let editMode    = false;

  // Caches référentiels
  let entreprisesCache = null; // [{id, nom}]
  let statutsCache     = null; // [{id, nom}]
  let profsCache       = null; // [{id, nom, prenom, login}]

  function loadEntreprises() {
    if (entreprisesCache) return Promise.resolve(entreprisesCache);
    return fetch(AppConfig.urlEnt, { headers: { 'Accept':'application/json' }})
      .then(r => { if (!r.ok) throw new Error('HTTP '+r.status); return r.json(); })
      .then(list => (entreprisesCache = list || []));
  }
  function loadStatuts() {
    if (statutsCache) return Promise.resolve(statutsCache);
    return fetch(AppConfig.urlStatut, { headers: { 'Accept':'application/json' }})
      .then(r => { if (!r.ok) throw new Error('HTTP '+r.status); return r.json(); })
      .then(list => (statutsCache = list || []));
  }
  function loadProfesseurs() {
    if (profsCache) return Promise.resolve(profsCache);
    return fetch(AppConfig.urlProf, { headers: { 'Accept':'application/json' }})
      .then(r => { if (!r.ok) throw new Error('HTTP '+r.status); return r.json(); })
      .then(list => (profsCache = list || []));
  }

  // DataTables
  const table = $('#tEtudiants').DataTable({
    ajax: {
      url: AppConfig.urlList,
      dataSrc: '',
      error: function(xhr){ console.error('Ajax DataTables:', xhr.status, xhr.responseText); }
    },
    columns: [
      { data: 'nom' },
      { data: 'prenom' },
      { data: 'typeStage' },
      { data: 'entreprise' }
    ],
    pageLength: 10,
    createdRow: function(row){ $(row).addClass('row-hover'); }
  });

  // Chargement initial: fiche du premier
  table.on('xhr', function(){
    const data = table.ajax.json();
    if (!Array.isArray(data) || data.length === 0) {
      if (currentId == null) {
        $('#ficheContent').html('<span class="muted">Aucune donnée à afficher.</span>');
      }
      return;
    }
    if (currentId == null) {
      $('#tEtudiants tbody tr').removeClass('selected');
      $('#tEtudiants tbody tr:eq(0)').addClass('selected');
      chargerFiche(data[0].id);
    }
  });

  // Clic ligne -> fiche
  $('#tEtudiants tbody').on('click', 'tr', function(){
    $('#tEtudiants tbody tr').removeClass('selected');
    $(this).addClass('selected');
    const rowData = table.row(this).data();
    if (rowData && rowData.id != null) chargerFiche(rowData.id);
  });

  // --- Fonctions utilitaires modernisées (avec Day.js) ---

  // Formatage d’affichage des dates ISO -> "YYYY-MM-DD HH:mm:ss"
  function formatDateTime(isoString) {
    if (!isoString) return '';
    // dayjs() parse les formats ISO nativement
    return dayjs(isoString).format('YYYY-MM-DD HH:mm:ss');
  }

  // Conversion des inputs date/time locaux vers un format ISO partiel pour le backend
  function toIsoLocal(dateStr, timeStr) {
    if (!dateStr) return null;
    // On combine la date et l'heure, puis on formate. Day.js gère les cas où timeStr est vide.
    return dayjs(`${dateStr} ${timeStr || '00:00'}`).format('YYYY-MM-DD HH:mm:ss');
  }

  // Pré-chargement des données de référence en arrière-plan pour une meilleure réactivité
  $(document).ready(function() {
      loadEntreprises();
      loadStatuts();
      loadProfesseurs();
  });

  // ----- Vue lecture -----
  function renderFicheView(e){
    const pres = (e.presentations ?? []).map(p => {
      const d = formatDateTime(p.datePresentee);
      const h = p.heure ? (String(p.heure).split('T')[1] || '').slice(0,5) : '';
      return `
        <li>
          <div><strong>Lieu :</strong> ${p.lieu ?? '-'}</div>
          <div><strong>Type de lieu :</strong> ${p.typeLieu ?? '-'}</div>
          <div><strong>Date :</strong> ${d || '-'}</div>
          <div><strong>Heure :</strong> ${h || '-'}</div>
        </li>`;
    }).join('') || '<li>Aucune présentation</li>';

    const jury = (e.jury ?? []).map(j =>
      `<li>${j.role ?? '-'} — ${j.nom ?? '-'} ${j.prenom ?? ''} (${j.login ?? '-'})</li>`
    ).join('') || '<li>Jury non défini</li>';

    $('#ficheContent').html(`
      <div class="section">
        <h3>Identité</h3>
        <div><strong>Nom:</strong> ${e.nom ?? '-'}</div>
        <div><strong>Prénom:</strong> ${e.prenom ?? '-'}</div>
      </div>

      <div class="section">
        <h3>Stage</h3>
        <div><strong>Titre:</strong> ${e.titre ?? '-'}</div>
        <div><strong>Type:</strong> ${e.typeStage ?? '-'}</div>
        <div><strong>Signée:</strong> ${e.signee === true ? 'Oui' : e.signee === false ? 'Non' : '-'}</div>
        <div><strong>Année:</strong> ${e.annee ?? '-'}</div>
        <div><strong>Entreprise:</strong> ${e.entreprise ?? '-'}</div>
        <div><strong>Début:</strong> ${formatDateTime(e.dateDebut) || '-'}</div>
        <div><strong>Fin:</strong> ${formatDateTime(e.dateFin) || '-'}</div>
      </div>

      <div class="section">
        <h3>Soutenance</h3>
        <div><strong>Statut:</strong> ${e.statut ?? '-'}</div>
        <div><strong>Confidentiel:</strong> ${e.confidentiel === true ? 'Oui' : e.confidentiel === false ? 'Non' : '-'}</div>
        <div><strong>Maître de stage présent:</strong> ${e.maitreStage === true ? 'Oui' : e.maitreStage === false ? 'Non' : '-'}</div>
        <div><strong>Réponse:</strong> ${e.reponse === true ? 'Oui' : e.reponse === false ? 'Non' : '-'}</div>
        <div><strong>Note:</strong> ${e.note ?? '-'}</div>
      </div>

      <div class="section">
        <h3>Présentations</h3>
        <ul>${pres}</ul>
      </div>

      <div class="section">
        <h3>Jury</h3>
        <ul>${jury}</ul>
      </div>
    `);

    $('#ficheActions').show();
    $('#btnEdit').show();
    $('#btnSave, #btnCancel').hide();
    editMode = false;
  }

  // ----- Vue édition -----
  function renderFicheEdit(e){
    $('#ficheContent').html('<span class="muted">Chargement des référentiels…</span>');
    Promise.all([loadEntreprises(), loadStatuts(), loadProfesseurs()])
    .then(([ents, stats, profs]) => {

      const optsEnt  = ['<option value="">-- Choisir --</option>']
        .concat(ents.map(x => `<option value="${x.id}">${x.nom}</option>`)).join('');
      const optsSta  = ['<option value="">-- Choisir --</option>']
        .concat(stats.map(x => `<option value="${x.id}">${x.nom}</option>`)).join('');
      const optsProf = profs.map(p =>
        `<option value="${p.id}">${p.nom} ${p.prenom} (${p.login})</option>`
      ).join('');

      // lignes de présentations existantes
      const presRows = (e.presentations ?? []).map(p => {
        const d = p.datePresentee ? String(p.datePresentee).split('T')[0] : '';            // yyyy-MM-dd
        const t = p.heure ? (String(p.heure).split('T')[1] || '').slice(0,5) : '';         // HH:mm
        return `
          <tr>
            <td><input type="date" class="inpPresDate" value="${d}"></td>
            <td><input type="time" class="inpPresHeure" value="${t}"></td>
            <td><input type="text" class="inpPresLieu" value="${p.lieu ?? ''}" placeholder="ex: Amphi A / Zoom"></td>
            <td>
              <select class="selPresType">
                <option value="">-</option>
                <option value="presentiel" ${p.typeLieu==='presentiel'?'selected':''}>presentiel</option>
                <option value="distanciel" ${p.typeLieu==='distanciel'?'selected':''}>distanciel</option>
                <option value="autre" ${p.typeLieu==='autre'?'selected':''}>autre</option>
              </select>
            </td>
            <td><button type="button" class="btnDelPres">Supprimer</button></td>
          </tr>`;
      }).join('');

      $('#ficheContent').html(`
        <div class="form-grid">
          <div class="section">
            <h3>Identité</h3>
            <label>Nom</label>
            <input id="inpNom" value="${e.nom ?? ''}">
            <label>Prénom</label>
            <input id="inpPrenom" value="${e.prenom ?? ''}">
          </div>

          <div class="section">
            <h3>Stage</h3>
            <label>Titre</label>
            <input id="inpTitre" value="${e.titre ?? ''}">
            <label>Début (yyyy-MM-dd HH:mm:ss)</label>
                  <input id="inpDateDebut" value="${formatDateTime(e.dateDebut) || ''}" placeholder="2025-10-25 22:46:14">
            <label>Fin (yyyy-MM-dd HH:mm:ss)</label>
                  <input id="inpDateFin" value="${formatDateTime(e.dateFin) || ''}" placeholder="2026-04-25 22:46:14">
            <label>Type</label>
            <input id="inpTypeStage" value="${e.typeStage ?? ''}">
            <label>Signée</label>
            <input type="checkbox" id="chkSignee" ${e.signee ? 'checked' : ''}>
            <label>Année</label>
            <input id="inpAnnee" value="${e.annee ?? ''}">
            <label>Entreprise</label>
            <select id="selEntreprise">${optsEnt}</select>
          </div>

          <div class="section">
            <h3>Soutenance</h3>
            <label>Confidentiel</label>
            <input type="checkbox" id="chkConf" ${e.confidentiel ? 'checked' : ''}>
            <label>Maître de stage présent</label>
            <input type="checkbox" id="chkMS" ${e.maitreStage ? 'checked' : ''}>
            <label>Réponse</label>
            <input type="checkbox" id="chkRep" ${e.reponse ? 'checked' : ''}>
            <label>Note</label>
            <input id="inpNote" value="${e.note ?? ''}" placeholder="ex: 14.5">
            <label>Statut</label>
            <select id="selStatut">${optsSta}</select>
          </div>
        </div>

        <div class="section">
          <h3>Présentations</h3>
          <table id="tblPres" class="display" style="width:100%">
            <thead>
              <tr><th>Date</th><th>Heure</th><th>Lieu</th><th>Type</th><th></th></tr>
            </thead>
            <tbody>
              ${presRows}
            </tbody>
          </table>
          <button type="button" id="btnAddPres">+ Ajouter une présentation</button>
        </div>

        <div class="section">
          <h3>Jury</h3>
          <label>Président</label>
          <select id="selPresident">
            <option value="">-- Choisir --</option>
            ${optsProf}
          </select>
          <label>Rapporteur</label>
          <select id="selRapporteur">
            <option value="">-- Choisir --</option>
            ${optsProf}
          </select>
        </div>
      `);

      // Pré-sélections
      if (e.entrepriseId) $('#selEntreprise').val(String(e.entrepriseId));
      if (e.statutId)     $('#selStatut').val(String(e.statutId));
      const juryList = e.jury ?? [];
      const pj = juryList.find(j => j.role === 'Président');
      const rj = juryList.find(j => j.role === 'Rapporteur');
      if (pj && pj.professeurId) $('#selPresident').val(String(pj.professeurId));
      if (rj && rj.professeurId) $('#selRapporteur').val(String(rj.professeurId));

      // Handlers Présentations
      $('#btnAddPres').on('click', function(){
        $('#tblPres tbody').append(`
          <tr>
            <td><input type="date" class="inpPresDate"></td>
            <td><input type="time" class="inpPresHeure"></td>
            <td><input type="text" class="inpPresLieu" placeholder="ex: Amphi A / Zoom"></td>
            <td>
              <select class="selPresType">
                <option value="">-</option>
                <option value="presentiel">presentiel</option>
                <option value="distanciel">distanciel</option>
                <option value="autre">autre</option>
              </select>
            </td>
            <td><button type="button" class="btnDelPres">Supprimer</button></td>
          </tr>
        `);
      });
      $('#tblPres').on('click', '.btnDelPres', function(){
        $(this).closest('tr').remove();
      });

      $('#btnEdit').hide();
      $('#btnSave, #btnCancel').show();
      editMode = true;

    }).catch(err => {
      console.error('Référentiels:', err);
      $('#ficheContent').html('<span class="muted">Impossible de charger les référentiels.</span>');
    });
  }

  function chargerFiche(id){
    currentId = id;
    $('#ficheContent').html('<span class="muted">Chargement…</span>');
    const url = AppConfig.urlBase + '/' + id + '/full';
    fetch(url, { headers: { 'Accept':'application/json' }})
      .then(r => { if (!r.ok) throw new Error('HTTP '+r.status); return r.json(); })
      .then(e => { currentData = e; renderFicheView(e); })
      .catch(err => {
        console.error('Erreur fiche full:', err);
        $('#ficheContent').html('<span class="muted">Impossible de charger la fiche.</span>');
      });
  }

  // Boutons
  $('#btnEdit').on('click', function(){ if (currentData) renderFicheEdit(currentData); });
  $('#btnCancel').on('click', function(){ if (currentData) renderFicheView(currentData); });
  $('#btnSave').on('click', function(){
    if (!currentId) return;

    // Jury
    const vPres = $('#selPresident').val();
    const vRapp = $('#selRapporteur').val();
    if (vPres && vRapp && vPres === vRapp) {
      alert('Le président et le rapporteur doivent être deux enseignants différents.');
      return;
    }
    const presidentId  = vPres ? parseInt(vPres, 10) : null;
    const rapporteurId = vRapp ? parseInt(vRapp, 10) : null;

    // Présentations -> payload
    const presList = [];
    $('#tblPres tbody tr').each(function(){
      const d    = $(this).find('.inpPresDate').val();
      const h    = $(this).find('.inpPresHeure').val();
      const lieu = $(this).find('.inpPresLieu').val()?.trim() || null;
      const type = $(this).find('.selPresType').val() || null;
      if (!d && !h && !lieu) return; // ignore ligne totalement vide
      presList.push({
        datePresentee: toIsoLocal(d, h),                  // "yyyy-MM-dd HH:mm:ss" ou null
        heure: h ? toIsoLocal('0001-01-01', h) : null,   // si tu gardes un champ heure séparé
        lieu: lieu,
        typeLieu: type
      });
    });

    const payload = {
      // Étudiant
      nom:        $('#inpNom').val()?.trim() || '',
      prenom:     $('#inpPrenom').val()?.trim() || '',
      // Stage
      titre:      $('#inpTitre').val()?.trim() || '',
      dateDebut:  $('#inpDateDebut').val()?.trim() || null,
      dateFin:    $('#inpDateFin').val()?.trim() || null,
      typeStage:  $('#inpTypeStage').val()?.trim() || null,
      signee:     $('#chkSignee').is(':checked'),
      annee:      $('#inpAnnee').val()?.trim() || null,
      entrepriseId: (function(){ const v = $('#selEntreprise').val(); return v ? parseInt(v,10) : null; })(),
      // Soutenance
      confidentiel: $('#chkConf').is(':checked'),
      maitreStage:  $('#chkMS').is(':checked'),
      reponse:      $('#chkRep').is(':checked'),
      note:         $('#inpNote').val()?.trim() || null,
      statutId: (function(){ const v = $('#selStatut').val(); return v ? parseInt(v,10) : null; })(),
      // Jury
      presidentId:  presidentId,
      rapporteurId: rapporteurId,
      // Présentations
      presentations: presList
    };

    $('#ficheContent').html('<span class="muted">Enregistrement…</span>');
    fetch(`${AppConfig.urlBase}/${currentId}/full`, {
      method: 'POST',
      headers: { 'Content-Type':'application/json', 'Accept':'application/json' },
      body: JSON.stringify(payload)
    })
    .then(r => {
      if (r.status === 400) { return r.text().then(t => Promise.reject(new Error(t))); }
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(e => {
      currentData = e;
      renderFicheView(e);
      table.ajax.reload(null, false); // garde la même page/tri
    })
    .catch(err => {
      console.error('Erreur save full:', err);
      $('#ficheContent').html('<span class="muted">Échec: ' + err.message + '</span>');
    });
  });
})();