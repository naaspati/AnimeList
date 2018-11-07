	// loadAnime(...)
		if(ids.size() == 1) {
			if(aboutBox.getChildren().get(0) == idsLinks)
				aboutBox.getChildren().remove(0);
			

			loader.getAnime(ids.get(0)).whenComplete((anime, ex) -> {
				if(anime == null || loader.isStopping())
					return;

				if(ex != null)
					FxAlert.showErrorDialog(null, ex.getMessage(), ex);
				else {
					runLater(() -> {
						FxPopupShop.hide(popup, 0);
						idBtn.setDisable(false);
						multipleIdBtn.setDisable(false);
						setCurrentAnime(anime);
						if(anime.isNew())
							dirSearchReset();
					});
				}
			});	
			return; 
		}

		if(aboutBox.getChildren().get(0) != idsLinks)
			aboutBox.getChildren().add(0, idsLinks);
		
		idsLinks.getChildren().clear();
		boolean first = true;

		for (Integer id : ids) {
			Hyperlink link = new Hyperlink(Integer.toString(id));
			link.setDisable(true);
			idsLinks.getChildren().add(link);

			boolean isFirst = first;
			first = false;

			loader.getAnime(id)
			.whenComplete((anime, ex) -> {
				if(anime == null || loader.isStopping())
					return;

				if(ex != null) {
					runLater(() -> {
						link.setOnAction(e -> FxAlert.showErrorDialog(null, ex.getMessage(), ex));
						link.setDisable(false);
						link.setUserData(null);
						link.setStyle("-fx-text-fill: red;");
					});
				} else {
					runLater(() -> {
						link.setDisable(false);
						link.setUserData(anime);
						link.setOnAction(this::setCurrentAnime);

						if(isFirst) {
							FxPopupShop.hide(popup, 0);
							link.fire();
						}
					});
				}
			});	
		}

		CompletableFuture.runAsync(() -> {
			runLater(() -> {
				idBtn.setDisable(false);
				multipleIdBtn.setDisable(false);
				if(!areAnimeRelated) {
					dirSearchReset();
					return;
				}
				
				for (Node node : idsLinks.getChildren()) {
					Anime anime = (Anime) node.getUserData();
					if(anime == null)
						continue;
					
					for (Node node2 : idsLinks.getChildren()) {
						Anime anime2 = (Anime) node2.getUserData();
						if(anime2 != null && anime2 != anime)
							anime.getRelatedAnimes().add(anime2);
					}
				}
				dirSearchReset();
			});
		}, loader.executor());